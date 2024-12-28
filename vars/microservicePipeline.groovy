import org.practicaldevops.template.FrontendPipeline
import org.practicaldevops.template.BackendPipeline

def call(Map config) {
    // If no script parameter is provided, use 'this'
    def scriptContext = config.script ?: this
    
    // Merge the provided config with defaults
    def pipelineConfig = [
        serviceType: config.type ?: 'backend',  // default to backend if not specified
        serviceName: config.name ?: "${config.type}-service",
        environment: config.environment ?: 'dev'
    ]

    def pipeline
    
    switch(pipelineConfig.serviceType) {
        case 'frontend':
            pipeline = new FrontendPipeline(this, config)
            break
        case 'backend':
            pipeline = new BackendPipeline(this, config)
            break
        default:
            error "Unsupported service type: ${config.serviceType}"
    }
    
    pipeline.execute()
}