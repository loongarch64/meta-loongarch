# meta-loongarch
这是用于loongarch架构的yocto bsp层。目前oe上游已部分支持loongarch架构，但仍然有些包不支持loongarch，我们对其进行了最小修改以支持loongarch（目前主要是qemuloongarch64机器），结合该层，我们可以构建一个可在qemu上运行的loongarch架构的操作系统以及对应的sdk。

## 为qemuloongarch64构建镜像的步骤

1. 从上游克隆poky/openembedded-core源码. 此处以poky为例
```bash
git clone https://git.yoctoproject.org/poky
```

2. 在poky目录下克隆该层源码
```bash
cd poky
git clone https://github.com/loongarch64/meta-loongarch.git
```

3. 设置构建环境
```bash
export MACHINE=qemuloongarch64
source oe-init-build-env
bitbake-layers add-layer ../meta-loongarch/
```

4. 根据文件meta-loongarch/poky-patch/poky.patch对mete层中的文件进行更改
	目前仍有一些补丁尚未提交给上游。今后将不需要该步骤。
```bash
cd .. && git apply meta-loongarch/poky-patch/poky.patch && cd -
```

5. 修改local.conf以适配loongarch设置。
```bash
cp ../meta-loongarch/conf/local.conf.sample conf/local.conf # 编译运行在qemu上的镜像
cp ../meta-loongarch/conf/local-iso.conf.sample conf/local.conf # 编译运行在真机上的iso镜像
```

6. 构建镜像. 提示中的所有common targets（包括sdk）现在都可以构建，例如：
```bash
bitbake core-image-minimal # minimal image
bitbake -c populate_sdk meta-toolchain # sdk
```
7. 启动
```bash
runqemu
```

## 提示
qemu-system-longarch64需要bios。目前，edk2-loongarch提供了它的二进制代码。未来应更改为从上游拉取生成。Poky有ovmf配方，但目前还不支持loongarch。
