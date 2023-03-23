#!/bin/bash
# Bootstrapper for buildbot slave

SELF_DIR=$(dirname `realpath $0`)

export LC_ALL=en_US.UTF-8
export BB_NUMBER_THREADS="10"
export PARALLEL_MAKE=" -j 10"

DIR="build"
MACHINE="qemuloongarch64"
CONFFILE="conf/auto.conf"
TARGET="core-image-minimal"
DL_DIR=$(dirname $SELF_DIR)/downloads

# bootstrap OE
echo "Init OE"
export BASH_SOURCE="openembedded-core/oe-init-build-env"
. ./openembedded-core/oe-init-build-env $DIR

# Symlink the downloads
if [ ! -L downloads ]; then
        ln -sf ${DL_DIR} downloads
fi

if [ -f conf/local.conf ]; then
	sed -i 's/qemux86-64/qemuloongarch64/' conf/local.conf
fi
# add the missing layers
echo "Adding layers"
bitbake-layers add-layer ../meta-loongarch

# fix the configuration
echo "Creating auto.conf"

if [ -e $CONFFILE ]; then
    rm -rf $CONFFILE
fi
cat <<EOF > $CONFFILE
MACHINE ?= "${MACHINE}"
CONF_VERSION = "2"

IMAGE_INSTALL:append = " edk2-loongarch"
KERNEL_IMAGETYPE:qemuloongarch64 = "vmlinux.efi"
QB_DEFAULT_BIOS:qemuloongarch64 = "QEMU_EFI.fd"
EXTRA_IMAGEDEPENDS:remove:qemuloongarch64 = "u-boot"
KERNEL_IMAGETYPES:remove:qemuloongarch64 = "vmlinuz"
QB_OPT_APPEND:qemuloongarch64 = "-device virtio-tablet-pci -device virtio-keyboard-pci"
QB_MEM:qemuloongarch64 = "-m 1024"
QB_NETWORK_DEVICE:qemuloongarch64 = "-device virtio-net-pci,netdev=net0,mac=@MAC@"
QB_ROOTFS_OPT:qemuloongarch64 = "-drive id=disk0,file=@ROOTFS@,if=none,format=raw -device virtio-blk-pci,drive=disk0"
QB_GRAPHICS:qemuloongarch64 = "-device virtio-vga -device qemu-xhci -device usb-kbd -device usb-mouse"
EOF

echo "To build an image run"
echo "---------------------------------------------------"
echo "MACHINE=qemuloongarch64 bitbake core-image-full-cmdline"
echo "---------------------------------------------------"
echo ""
echo "Buildable machine info"
echo "---------------------------------------------------"
echo "* qemuloongarch64: The 64-bit LoongArch machine"
echo "---------------------------------------------------"
echo "Common targets are:"
echo "    core-image-minimal"
echo "    core-image-full-cmdline"
echo "    core-image-sato"
echo "    core-image-weston"
echo "    meta-toolchain"
echo "    meta-ide-support"
echo "---------------------------------------------------"

# start build
echo "Starting build"
bitbake -vDDD ${TARGET} 2>&1 | tee ../build.log
