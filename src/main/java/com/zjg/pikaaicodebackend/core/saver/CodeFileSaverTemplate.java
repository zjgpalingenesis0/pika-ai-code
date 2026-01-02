package com.zjg.pikaaicodebackend.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.zjg.pikaaicodebackend.exception_.ThrowUtils;
import com.zjg.pikaaicodebackend.model.enums_.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static com.zjg.pikaaicodebackend.constant.AppConstant.CODE_OUTPUT_ROOT_DIR;
import static com.zjg.pikaaicodebackend.exception_.ErrorCode.PARAMS_ERROR;
import static com.zjg.pikaaicodebackend.exception_.ErrorCode.SYSTEM_ERROR;

/**
 * 抽象代码文件保存器--模板方法模式
 * @param <T>
 */
public abstract class CodeFileSaverTemplate<T> {

    /**
     * 文件保存根目录
     */
    protected static final String FILE_SAVE_ROOT_DIR = CODE_OUTPUT_ROOT_DIR;

    /**
     * 模板方法--保存代码的标准流程
     * @param result  代码结果对象
     * @return
     */
    public final File saveFile(T result, Long appId) {
        //验证输入
        validInput(result);
        //构建唯一目录
        String baseDirPath = buildUniqueDir(appId);
        //保存文件(子类要实现的抽象方法)
        saveFiles(result, baseDirPath);
        //返回文件对象
        return new File(baseDirPath);
    }

    /**
     * 校验输入
     * @param result
     */
    protected void validInput(T result) {
        ThrowUtils.throwIf(result == null, SYSTEM_ERROR, "代码结果不能为空");
    }

    /**
     * 构建唯一目录路径
     * @return
     */
    protected final String buildUniqueDir(Long appId) {
        //校验
        ThrowUtils.throwIf(appId == null || appId <= 0, PARAMS_ERROR);
        String codeType = getCodeType().getValue();
        String uniqueDirName = String.format("%s_%s", codeType, appId);
        //完整路径
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件夹
     * @param dirPath
     * @param fileName
     * @param content
     */
    protected final void writeToFile(String dirPath, String fileName, String content) {
        if (StrUtil.isNotBlank(content)) {
            String filePath = dirPath + File.separator + fileName;
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }

    }

    /**
     * 保存文件
     * @param result
     * @param baseDirPath
     */
    protected abstract void saveFiles(T result, String baseDirPath);

    /**
     * 获取代码生成类型
     * @return
     */
    protected abstract CodeGenTypeEnum getCodeType();

}
