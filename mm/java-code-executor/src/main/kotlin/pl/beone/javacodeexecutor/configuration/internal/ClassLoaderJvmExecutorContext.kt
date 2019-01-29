package pl.beone.javacodeexecutor.configuration.internal

import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.javacodeexecutor.internal.ClassLoaderJvmExecutor

@Configuration
class ClassLoaderJvmExecutorContext {

    @Bean
    fun classLoaderJvmExecutor(autowireCapableBeanFactory: AutowireCapableBeanFactory) =
            ClassLoaderJvmExecutor(autowireCapableBeanFactory)
}