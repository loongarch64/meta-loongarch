require gcc-common.inc

# Third digit in PV should be incremented after a minor release

PV = "13.0.1"

# BINV should be incremented to a revision after a minor gcc release

BINV = "13.0.1"

FILESEXTRAPATHS =. "${FILE_DIRNAME}/gcc:${FILE_DIRNAME}/gcc/backport:"

DEPENDS =+ "mpfr gmp libmpc zlib flex-native"
NATIVEDEPS = "mpfr-native gmp-native libmpc-native zlib-native flex-native zstd-native"

LICENSE = "GPL-3.0-with-GCC-exception & GPL-3.0-only"

LIC_FILES_CHKSUM = "\
    file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552 \
    file://COPYING3;md5=d32239bcb673463ab874e80d47fae504 \
    file://COPYING3.LIB;md5=6a6a8e020838b23406c81b19c1d46df6 \
    file://COPYING.LIB;md5=2d5025d4aa3495befef8f17206a5b0a1 \
    file://COPYING.RUNTIME;md5=fe60d87048567d4fe8c8a0ed2448bcc8 \
"

# official release
RELEASE ?= "${PV}"
SOURCEDIR ?= "gcc-${PV}"

SRC_URI = "git://github.com/gcc-mirror/gcc.git;branch=master;depth=1;protocol=https "
SRCREV = "${AUTOREV}"

S = "${TMPDIR}/work-shared/gcc-${PV}-${PR}/git"
B = "${WORKDIR}/gcc-${PV}/build.${HOST_SYS}.${TARGET_SYS}"

# Language Overrides
FORTRAN = ""
JAVA = ""

SSP ?= "--disable-libssp"
SSP:mingw32 = "--enable-libssp"

EXTRA_OECONF_BASE = "\
    ${SSP} \
    --enable-libitm \
    --enable-lto \
    --disable-bootstrap \
    --with-system-zlib \
    ${@'--with-linker-hash-style=${LINKER_HASH_STYLE}' if '${LINKER_HASH_STYLE}' else ''} \
    --enable-linker-build-id \
    --with-ppl=no \
    --with-cloog=no \
    --enable-checking=release \
    --enable-cheaders=c_global \
    --without-isl \
"

EXTRA_OECONF_INITIAL = "\
    --disable-libgomp \
    --disable-libitm \
    --disable-libquadmath \
    --with-system-zlib \
    --disable-lto \
    --disable-plugin \
    --enable-linker-build-id \
    --enable-decimal-float=no \
    --without-isl \
    --disable-libssp \
"

EXTRA_OECONF_PATHS = "\
    --with-gxx-include-dir=/not/exist{target_includedir}/c++/${BINV} \
    --with-sysroot=/not/exist \
    --with-build-sysroot=${STAGING_DIR_TARGET} \
"

# Is a binutils 2.26 issue, not gcc
CVE_CHECK_IGNORE += "CVE-2021-37322"