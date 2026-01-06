package com.zjg.pikaaicodebackend.ai.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static com.zjg.pikaaicodebackend.constant.AppConstant.CODE_OUTPUT_ROOT_DIR;

@Slf4j
public class FileWriteTool {

    @Tool("写入文件到指定路径")
    public String writeFile(@P("文件相对路径") String relativeFilePath,
                            @P("文件要写入的内容") String content,
                            @ToolMemoryId Long appId) {
        try {
            //从相对路径中获取path
            Path path = Paths.get(relativeFilePath);
            //如果不是绝对路径，基于相对路径，创建基于appId的项目目录
            if (!path.isAbsolute()) {
                /**
                 * 假设:
                 *   - appId = 123
                 *   - CODE_OUTPUT_ROOT_DIR = F:/code_output
                 *   - relativeFilePath = src/components/Header.vue
                 *
                 *   执行过程:
                 *   1. projectDirName = "vue_project123"
                 *   2. projectRoot = F:/code_output/vue_project123
                 *   3. path =
                 *   F:/code_output/vue_project123/src/components/Header.vue
                 */
                String projectDirName = "vue_project" + appId;
                Path projectRoot = Paths.get(CODE_OUTPUT_ROOT_DIR, projectDirName);
                path = projectRoot.resolve(relativeFilePath);
            }
            //创建父目录
            Path parentDir = path.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            //写入文件内容
            Files.write(path, content.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            log.info("成功写入文件，{}", path.toAbsolutePath());
            return "文件写入成功: " + relativeFilePath;
        } catch (IOException e) {
            String errorMessage = "文件写入失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }
}
