
# 基于开源版Nacos，在安全防护方面进行加固，以适用于对安全防护要求比较高的场景。

[![Gitter](https://badges.gitter.im/alibaba/nacos.svg)](https://gitter.im/alibaba/nacos?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)   [![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Gitter](https://travis-ci.org/alibaba/nacos.svg?branch=master)](https://travis-ci.org/alibaba/nacos)
[![](https://img.shields.io/badge/Nacos-Check%20Your%20Contribution-orange)](https://opensource.alibaba.com/contribution_leaderboard/details?projectValue=nacos)

-------

## 安全加固后的版本已用于公司实际生产环境，通过了专业网络攻击测试及绿盟扫描。

1. 本项目基于Ncos2.3.0改造，后续会根据Nacos基线随时更新。
2. 本项目使用了Nacos插件cipher-aes 加密功能， 下载本项目前先下载 https://github.com/nacos-group/nacos-plugin.git
      用 IDEA 打开这个插件项目,执行 install 操作，将所有的插件都安装到本地仓库。接下来回到一开始的 Nacos 项目中，在 Nacos 项目中引入这个插件的依赖，建议在 config 模块中引入。
   加了这个依赖之后，我们的 Nacos 就具备了配置文件加密功能了。引入内容如下：
   ``` 
   <dependency>
       <groupId>com.alibaba.nacos</groupId>
       <artifactId>nacos-aes-encryption-plugin</artifactId>
       <version>1.0.0-SNAPSHOT</version>
   </dependency>
   ``` 
   
3. 源码构建：在nacos目录下执行：
   `mvn -Prelease-nacos clean install -Dmaven.test.skip=true -Dcheckstyle.skip=true -Dpmd.skip=true -Drat.skip=true -U`
   构建好的Nacos部署包在 distribution 工程下。
