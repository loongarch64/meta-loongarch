# meta-loongarch

这是用于loongarch架构的yocto bsp层。目前oe上游已部分支持loongarch架构，但仍然有些包不支持loongarch，我们对其进行了最小修改以支持loongarch（目前主要是qemuloongarch64机器），结合该层，我们可以构建一个可在qemu上运行的loongarch架构的操作系统以及对应的sdk。

## 为qemuloongarch64构建镜像的步骤

```
mkdir -p loong-yocto/downloads
cd loong-yocto
git clone https://github.com/openembedded/bitbake.git
git clone https://github.com/openembedded/openembedded-core.git
git clone https://github.com/loongarch64/meta-loongarch.git
./meta-loongarch/build.sh
```

如果要使用 `poky`，请参考以下内容：

```
cd loong-yocto
git clone https://git.yoctoproject.org/poky.git
sed -i 's/openembedded-core/poky/g' meta-loongarch/build.sh
./meta-loongarch/build.sh
```

## 提示

qemu-system-longarch64需要bios。目前，edk2-loongarch提供了它的二进制代码。未来应更改为从上游拉取生成。Poky有ovmf配方，但目前还不支持loongarch。
