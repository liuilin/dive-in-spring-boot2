package com.liumulin.annotation;

import com.liumulin.configuration.HelloWorldConfiguration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author liuqiang
 * @since 2022-02-18
 */
public class HelloWorldImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        // 可以选择加分支，更加灵活
        return new String[]{HelloWorldConfiguration.class.getName()};
    }
}
