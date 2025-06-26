FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://openssl-${PV}-loongarch-support.patch"
