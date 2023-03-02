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

