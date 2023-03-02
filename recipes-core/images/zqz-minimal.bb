SUMMARY = "A small image just capable of allowing a device to boot."

#IMAGE_INSTALL = "packagegroup-core-boot ${CORE_IMAGE_EXTRA_INSTALL}"
IMAGE_INSTALL = "packagegroup-core-boot grub-efi"
IMAGE_FEATURES += "splash package-management ssh-server-dropbear hwcodecs weston"
CORE_IMAGE_BASE_INSTALL += "gtk+3-demo"
CORE_IMAGE_BASE_INSTALL += "${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'weston-xwayland matchbox-terminal', '', d)}"
IMAGE_LINGUAS = " "
PV = "MACOS"
LICENSE = "MIT"

inherit core-image
MACHINE_FEATURES += "efi "

populate_kernel() {
    dest=$1
    install -d $dest

    cat > $dest/boot.cfg << EOF
timeout 5
default 0
showmenu 1
EOF

    if [ -f $dest/grub.cfg ]; then
        rm $dest/grub.cfg
    fi
    touch $dest/grub.cfg
    cat > $dest/grub.cfg << EOF
serial --unit=0 --speed=115200 --word=8 --parity=no --stop=1
default=1
timeout=5
EOF

# Install bzImage, initrd, and rootfs.img in DEST for all loaders to use.
bbnote "Trying to install ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGETYPE} as $dest/${KERNEL_IMAGETYPE}"
if [ -e ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGETYPE} ]; then
    install -m 0644 ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGETYPE} $dest/${KERNEL_IMAGETYPE}
fi
for kernel in `find ${DEPLOY_DIR_IMAGE} -name ${KERNEL_IMAGETYPE}*.bin -type f`; do
    kname_orig=`basename $kernel .bin`
    kname=`echo ${kname_orig} | sed 's/${KERNEL_IMAGETYPE}/ls/g' | sed 's/-//g'`
    if [ "$kname" != "${kname/rt/}" ] ; then
        continue
    fi
    install -m 0644 ${kernel} $dest/${kname}
    cat >> $dest/boot.cfg << EOF

title LoongOS for live $kname (USB)
    kernel /dev/fs/iso9660@usb0/$kname
    initrd /dev/fs/iso9660@usb0/initrd
    args root=/dev/ram0 LABEL=boot rootfstype=auto ro rd.live.image quiet splash pmon=true

title LoongOS for $kname (USB)
    kernel /dev/fs/iso9660@usb0/$kname
    initrd /dev/fs/iso9660@usb0/initrd
    args root=/dev/ram0 LABEL=mtd rootfstype=auto ro rd.live.image quiet splash pmon=true

title LoongOS for $kname (SATA-CD)
    kernel /dev/fs/iso9660@cd0/$kname
    initrd /dev/fs/iso9660@cd0/initrd
    args root=/dev/ram0 LABEL=mtd rootfstype=auto ro rd.live.image quiet splash pmon=true
EOF
    cat >> $dest/grub.cfg << EOF
menuentry "LoongOS for $kname liveCD" \{
linux /$kname LABEL=boot root=/dev/ram0
initrd /initrd
\}
menuentry "install LoongOS for $kname" \{
linux /$kname LABEL=install-efi root=/dev/ram0
initrd /initrd
\}
EOF
done
sed -i 's#\\##g' $dest/grub.cfg
if [ ! -d $dest/EFI/BOOT ]; then
    mkdir -p $dest/EFI/BOOT
fi
cp $dest/grub.cfg $dest/EFI/BOOT

# initrd is made of concatenation of multiple filesystem images
if [ -n "${INITRD}" ]; then
    rm -f $dest/initrd
    for fs in ${INITRD}
    do
        if [ -s "$fs" ]; then
            cat $fs >> $dest/initrd
        else
            bbfatal "$fs is invalid. initrd image creation failed."
        fi
    done
    chmod 0644 $dest/initrd
fi
}
addtask generate_bootcfg after do_rootfs before do_image
do_generate_bootcfg() {
    cd ${WORKDIR}/rootfs/boot
    if [ -f "boot.cfg" ]; then
        rm boot.cfg
    fi
    touch boot.cfg
    cat > boot.cfg << EOF
timeout 1
default 0
showmenu 1
EOF

for kernel in `find . -type f -name '${KERNEL_IMAGETYPE}*' `; do
    kname=`basename $kernel`
    cat >> boot.cfg << EOF

title LoongOS ${kname}
    kernel /dev/fs/ext2@wd0/boot/${kname}
    args  root=/dev/sda1 ro rhgb quiet loglevel=0 LANG=zh_CN.UTF-8
EOF
done
}
# efi_populate_common DEST BOOTLOADER
efi_populate_common() {
        # DEST must be the root of the image so that EFIDIR is not
        # nested under a top level directory.
        DEST=$1

        install -d ${DEST}${EFIDIR}

        install -m 0644 ${DEPLOY_DIR_IMAGE}/BOOTL.EFI ${DEST}${EFIDIR}/BOOTL.EFI
        EFIPATH=$(echo "${EFIDIR}" | sed 's/\//\\/g')
        printf 'fs0:%s\%s\n' "$EFIPATH" BOOTL.EFI >${DEST}/startup.nsh
}
efi_populate() {
        efi_populate_common "$1" grub-efi

        install -m 0644 ${GRUB_CFG} ${DEST}${EFIDIR}/grub.cfg
}

efi_iso_populate() {
        iso_dir=$1
        efi_populate $iso_dir
        # Build a EFI directory to create efi.img
        mkdir -p ${EFIIMGDIR}/${EFIDIR}
        cat > $iso_dir/${EFIDIR}/grub.cfg << EOF
serial --unit=0 --speed=115200 --word=8 --parity=no --stop=1
default=1
timeout=5
EOF
        for kernel in `find ${DEPLOY_DIR_IMAGE} -name ${KERNEL_IMAGETYPE}*.bin -type f`; do
            kname_orig=`basename $kernel .bin`
            kname=`echo ${kname_orig} | sed 's/${KERNEL_IMAGETYPE}/ls/g' | sed 's/-//g'`
            install -m 0644 ${kernel} ${EFIIMGDIR}/${kname}
            cat >> $iso_dir/${EFIDIR}/grub.cfg << EOF
menuentry "LoongOS for $kname liveCD" \{
linux /$kname LABEL=boot root=/dev/ram0
initrd /initrd
\}
menuentry "install LoongOS for $kname" \{
linux /$kname LABEL=install-efi root=/dev/ram0
initrd /initrd
\}
EOF
        done
        sed -i 's#\\##g' $iso_dir/${EFIDIR}/grub.cfg
        cp $iso_dir/${EFIDIR}/* ${EFIIMGDIR}${EFIDIR}

        EFIPATH=$(echo "${EFIDIR}" | sed 's/\//\\/g')
        printf 'fs0:%s\%s\n' "$EFIPATH" BOOTL.EFI >${EFIIMGDIR}/startup.nsh

        if [ -f "$iso_dir/initrd" ] ; then
                cp $iso_dir/initrd ${EFIIMGDIR}
        fi
}
