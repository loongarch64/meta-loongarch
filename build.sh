#!/bin/bash

# Bootstrapper for buildbot slave
set -e
SELF_DIR=$(dirname `realpath $0`)

export LC_ALL=en_US.UTF-8
export BB_NUMBER_THREADS="`nproc`"
export PARALLEL_MAKE=" -j `nproc`"

DIR="build"
MACHINE="qemuloongarch64"
CONFFILE="conf/auto.conf"
TARGET=${TARGET:-core-image-minimal}
DL_DIR=$(dirname $SELF_DIR)/downloads

# bootstrap OE
if [[ -n "$DISTRO" && -d "$DISTRO" ]]; then
	if [ -f ./$DISTRO/oe-init-build-env ]; then
		DISTRO=`basename $DISTRO`
	else
		DISTRO="openembedded-core"
	fi
else
	if [ -d openembedded-core ]; then
		DISTRO="openembedded-core"
	elif [ -d poky ]; then
		DISTRO="poky"
	fi
fi

echo "Init OE for $DISTRO"
export BASH_SOURCE="$DISTRO/oe-init-build-env"
. ./$DISTRO/oe-init-build-env $DIR

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

IMAGE_INSTALL:append = " edk2"
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

# start build
if [ $# -eq 0 ];then
	echo "To build an image run"
	echo "---------------------------------------------------"
	echo "MACHINE=qemuloongarch64 bitbake $TARGET"
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
	echo "Starting build..."
	echo "$ bitbake -vDDD ${TARGET}"
	read -p "Continue[Y/n]? " -n 1 -r -t 5 || { err=$?; if [ $err -gt 128 ] ; then REPLY=y; fi }
	echo
	if [[ ! $REPLY =~ ^[Yy]$ ]] && [ ! -z $REPLY ]; then
		[[ "$0" = "$BASH_SOURCE" ]] && exit 1 || return 1
	fi

	bitbake -vDDD ${TARGET} 2>&1 | tee ../build.log
	if [ ${PIPESTATUS[0]} -eq 0 ]; then
		runqemu nographic
	fi
elif [ $# -eq 1 ];then
        if echo $1 | grep ":";then
                items=(${1//:/ })
                echo "Starting build..."
                echo "$ bitbake -vDDD -b ${items[0]} -c ${items[1]}"
		read -p "Continue[Y/n]? " -n 1 -r -t 5 || { err=$?; if [ $err -gt 128 ] ; then REPLY=y; fi }
		echo
		if [[ ! $REPLY =~ ^[Yy]$ ]] && [ ! -z $REPLY ]; then
			[[ "$0" = "$BASH_SOURCE" ]] && exit 1 || return 1
		fi
		unset BB_NUMBER_THREADS PARALLEL_MAKE
                bitbake -vDDD -b ${items[0]} -c ${items[1]} 2>&1 | tee ../one.log
        else
                echo "Starting build..."
                echo "$ bitbake -vDDD -b $1"
                read -p "Continue[Y/n]? " -n 1 -r -t 5 || { err=$?; if [ $err -gt 128 ] ; then REPLY=y; fi }
		echo
		if [[ ! $REPLY =~ ^[Yy]$ ]] && [ ! -z $REPLY ]; then
			[[ "$0" = "$BASH_SOURCE" ]] && exit 1 || return 1
		fi
		unset BB_NUMBER_THREADS PARALLEL_MAKE
                bitbake -vDDD -b $1 2>&1 | tee ../one.log
        fi
elif [ $# -eq 2 ];then
	echo "Starting build..."
        echo "$ bitbake -vDDD -b $1 -c $2"
	read -p "Continue[Y/n]? " -n 1 -r -t 5 || { err=$?; if [ $err -gt 128 ] ; then REPLY=y; fi }
	echo
	if [[ ! $REPLY =~ ^[Yy]$ ]] && [ ! -z $REPLY ]; then
		[[ "$0" = "$BASH_SOURCE" ]] && exit 1 || return 1
	fi
	unset BB_NUMBER_THREADS PARALLEL_MAKE
        bitbake -vDDD -b $1 -c $2 2>&1 | tee ../one.log
fi
