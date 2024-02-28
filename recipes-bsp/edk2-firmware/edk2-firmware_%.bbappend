COMPATIBLE_MACHINE:qemuloongarch64 = "qemuloongarch64"
EDK2_PLATFORM:qemuloongarch64      = "LoongArchQemu"
EDK2_PLATFORM_DSC:qemuloongarch64  = "Platform/Loongson/LoongArchQemuPkg/Loongson.dsc"
EDK2_BIN_NAME:qemuloongarch64      = "QEMU_EFI.fd"
FILES:${PN} += "/QEMU_EFI.fd"

do_install:append:qemuloongarch64() {
    install ${B}/Build/${EDK2_PLATFORM}/${EDK2_BUILD_MODE}_${EDK_COMPILER}/FV/${EDK2_BIN_NAME} ${D}/QEMU_EFI.fd
}
#
