import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import java.util.HashMap;
import java.util.Map;

import static java.lang.StringTemplate.STR;

public static void main(String[] args) {

    // 1️⃣ 数据源配置
    DataSourceConfig dataSourceConfig = new DataSourceConfig
            .Builder("jdbc:mysql://localhost:3306/demo?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8", "root", "katelinna")
            .build();

    // 2️⃣ 创建生成器
    AutoGenerator generator = new AutoGenerator(dataSourceConfig);

    // 3️⃣ 全局配置
    GlobalConfig globalConfig = new GlobalConfig.Builder()
            .author("fan")
            // ⚠️注意：如果你的项目根目录就是 blog，这里可能只需要 "/src/main/java"
            // 根据你截图的结构，你的模块好像是在根目录下的 blog 文件夹里，所以保持现状即可
            .outputDir(System.getProperty("user.dir") + "/src/main/java")
            .commentDate("yyyy-MM-dd")
            .enableSwagger() // 开启 Swagger 模式 (可选)
            .disableOpenDir() // 生成完不打开文件夹
            .build();
    generator.global(globalConfig);

    // 4️⃣ 包配置
    Map<OutputFile, String> pathInfo = new HashMap<>();
    // XML 放在 resources/mapper 下
    pathInfo.put(OutputFile.xml, STR."\{System.getProperty("user.dir")}/src/main/resources/mapper");

    PackageConfig packageConfig = new PackageConfig.Builder()
            .parent("com.tayutadeshi.keshe") // 父包名
            .entity("pojo")     // 实体类包名 -> com.blog.pojo
            .mapper("mapper")   // Mapper包名 -> com.blog.mapper
            .service("service")
            .serviceImpl("service.impl")
            .controller("controller")
            .pathInfo(pathInfo)
            .build();
    generator.packageInfo(packageConfig);

    // 5️⃣ 策略配置
    StrategyConfig strategyConfig = new StrategyConfig.Builder()
            // 🔥修改点：把所有表都加上
            .addInclude("exam_item", "registration", "sys_user,sys_enrollment")

            // 🔥修改点：删除 .addTablePrefix("t_")，因为你的表没有前缀

            .entityBuilder()
            .enableLombok() // 开启 Lombok
            // 🔥重要修改：数据库是自增，这里必须是 AUTO
            .idType(IdType.AUTO)
            .naming(NamingStrategy.underline_to_camel)
            .columnNaming(NamingStrategy.underline_to_camel)
            // .enableTableFieldAnnotation() // 建议开启，生成 @TableField 注解

            .controllerBuilder()
            .enableRestStyle() // 开启 @RestController
            .enableHyphenStyle() // 开启驼峰转连字符
            .build();
    generator.strategy(strategyConfig);

    // 6️⃣ 执行生成
    generator.execute();

    System.out.println("✅ 代码生成完成！");
}