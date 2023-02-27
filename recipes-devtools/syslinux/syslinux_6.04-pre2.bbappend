COMPATIBLE_HOST:class-target = '(x86_64|i.86|loongarch).*-(linux|freebsd.*)'
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://isolinux.bin \
    file://0001-complie-isohybrid-for-loongson.patch "

do_configure() {
	oe_runmake firmware="elf64" clean
}

do_compile() {
	oe_runmake firmware="elf64" installer
}

do_install() {
	install -d ${D}${bindir}
	install \
		${B}/bios/mtools/syslinux \
		${B}/bios/extlinux/extlinux \
		${B}/bios/utils/isohybrid \
		${D}${bindir}
}

#
# Tasks for target which ship the precompiled bootloader and installer
#
do_configure:class-target() {
	# No need to do anything as we're mostly shipping the precompiled binaries
	:
}

do_compile:class-target() {
	# No need to do anything as we're mostly shipping the precompiled binaries
	:
}

do_install:class-target() {
	oe_runmake firmware="elf64" install INSTALLROOT="${D}"

	install -d ${D}${datadir}/syslinux/
    install -m 644 ${WORKDIR}/isolinux.bin ${D}${datadir}/syslinux/
	install -m 644 ${S}/bios/core/ldlinux.sys ${D}${datadir}/syslinux/
	install -m 644 ${S}/bios/core/ldlinux.bss ${D}${datadir}/syslinux/
}

INSANE_SKIP_${PN} = "already-stripped"
