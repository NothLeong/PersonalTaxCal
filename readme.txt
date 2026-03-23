姓名:梁仲禧
学号:23336129
课程：编译原理实验
实验：Lab0 个人所得税计算器

代码目录说明：
  src/main/java/Main.java
    用户交互入口，提供命令行交互界面.

  src/main/java/TaxCal.java
    个人所得税计算器核心类，实现起征点、税级、税表的配置与税款计算逻辑

  src/test/java/TaxCalTest.java
    TaxCal类的单元测试，覆盖正常计算、边界情况、异常处理等场景

  src/test/java/MainTest.java
    Main类的集成测试，覆盖用户交互流程、指令处理、异常输入等场景

  doc
    文档目录，内有面向对象程序设计文档 design.pdf 和javadoc生成的说明性文档

  pom.xml
    Maven构建配置文件，包含JUnit 5测试依赖

  run.bat
    程序编译与运行脚本，需JDK 17+
    运行方式:打开终端并在目录下执行:run.bat

  test.bat
    测试运行脚本，需要JDK 17+和Maven 3+
    运行方式:打开终端并在目录下执行:test.bat
    每次完成代码修改后应运行该文件以进行回归测试


