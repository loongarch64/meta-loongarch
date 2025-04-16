SUMMARY = "edk2 for loongarch recipe"
DESCRIPTION = "UEFI firmware for LoongArch64 virtual machines qemu-efi-loongarch64 is a build of EDK II for LoongArch64 virtual machines."
HOMEPAGE = "http://www.tianocore.org"
SECTION = "misc"

LICENSE = "BSD-2-Clause"
LIC_FILES_CHKSUM = "file://copyright;md5=d77014bd15221e04f8548d121eb5c832"

# Use QEMU_EFI.fd from debian deb package.
SRC_URI = "${DEBIAN_MIRROR}/main/e/${BPN}/qemu-efi-loongarch64_${PV}_all.deb"
SRC_URI[sha256sum] = "183d90b6f9fb88156987462ec4b7c3d21e20d0a3cfec809246c7135a5412b78a"

inherit deploy

FILES:${PN} += "${datadir}/qemu-efi-loongarch64/"

# Put the copyright into source directory from the debian package.
put_copyright() {
    install -Dm 0644 ${UNPACKDIR}/usr/share/doc/qemu-efi-loongarch64/copyright ${S}/copyright
}
do_unpack[postfuncs] += "put_copyright"

do_install () {
    install -d ${D}/${datadir}
    install -d ${D}/${datadir}/qemu-efi-loongarch64/
    install -Dm 0644 ${UNPACKDIR}/usr/share/qemu-efi-loongarch64/*.fd ${D}/${datadir}/qemu-efi-loongarch64
}

do_deploy () {
    install ${UNPACKDIR}/usr/share/qemu-efi-loongarch64/QEMU_EFI.fd ${DEPLOYDIR}/QEMU_EFI.fd
    install ${UNPACKDIR}/usr/share/qemu-efi-loongarch64/QEMU_VARS.fd ${DEPLOYDIR}/QEMU_VARS.fd
}

addtask do_deploy after do_compile

BBCLASSEXTEND = "native"
