package org.example.config;

import org.flowable.common.engine.impl.persistence.StrongUuidGenerator;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {

    @Override
    public void configure(SpringProcessEngineConfiguration config) {
        // 使用 StrongUuidGenerator 生成全局唯一 ID，避免集群冲突
        config.setIdGenerator(new StrongUuidGenerator());

        // 解决流程图中文乱码
        config.setActivityFontName("宋体");
        config.setLabelFontName("宋体");
        config.setAnnotationFontName("宋体");

        // 异步历史数据写入，提高流程运行时性能
        config.setAsyncHistoryEnabled(true);
        config.setAsyncHistoryExecutorActivate(true);

        // 启用流程定义信息缓存
        config.setEnableProcessDefinitionInfoCache(true);
    }
}
