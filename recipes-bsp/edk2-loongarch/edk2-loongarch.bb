SUMMARY = "UEFI EDK2 Firmware"
DESCRIPTION = "EDK2 for loongarch"

LICENSE = "BSD-2-Clause-Patent"
LIC_FILES_CHKSUM = "file://edk2/License.txt;md5=2b415520383f7964e96700ae12b4570a \
                    file://edk2-platforms/License.txt;md5=2b415520383f7964e96700ae12b4570a \
                    "

SRC_URI += "gitsm://github.com/tianocore/edk2;branch=master;protocol=https;name=edk2;destsuffix=git/edk2"
SRC_URI += "gitsm://github.com/tianocore/edk2-platforms;branch=master;protocol=https;name=edk2-platforms;destsuffix=git/edk2-platforms"
SRC_URI += "gitsm://github.com/tianocore/edk2-non-osi;branch=master;protocol=https;name=edk2-non-osi;destsuffix=git/edk2-non-osi"

SRCREV_edk2 = "33deaa3b845f0d588ffd068003558be46f90aaac"
SRCREV_edk2-platforms = "899a9dc97cd54690513380ad01ee8b2609dbefd5"
SRCREV_edk2-non-osi = "ddae61c7547dba5b6aae060917896a2d4271fa32"

SRC_URI += "https://github.com/loongson/build-tools/releases/download/2023.08.08/CLFS-loongarch64-8.1-x86_64-cross-tools-gcc-glibc.tar.xz"
SRC_URI[sha256sum] = "cd7c98499e1d7476df144cca22ade2140d3f311be0b4204591bdf8466971ba27"

SRCREV_FORMAT = "ekd2"

S = "${WORKDIR}/git"

inherit deploy

do_compile:class-native () {
    cd ${S}
    export PATH=${WORKDIR}/cross-tools/bin:$PATH
    export WORKSPACE=${S}
    export PACKAGES_PATH=${S}/edk2:${S}/edk2-platforms:${S}/edk2-non-osi
    export GCC5_LOONGARCH64_PREFIX=loongarch64-unknown-linux-gnu-
    . edk2/edksetup.sh
    make -C edk2/BaseTools

    build --buildtarget=RELEASE --tagname=GCC5 --arch=LOONGARCH64  --platform=Platform/Loongson/LoongArchQemuPkg/Loongson.dsc
}

do_deploy () {
    install ${S}/Build/LoongArchQemu/RELEASE_GCC5/FV/QEMU_EFI.fd ${DEPLOYDIR}/QEMU_EFI.fd
}

addtask do_deploy after do_compile

BBCLASSEXTEND = "native"