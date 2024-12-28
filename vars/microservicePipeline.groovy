import org.practicaldevops.templates.FrontendPipeline
import org.practicaldevops.templates.BackendPipeline

def call(Map config = [:]) {
    def scriptContext = config.script ?: this
    def pipelineConfig = [
        serviceType: config.type ?: 'backend',
        serviceName: config.name ?: "${config.type}-service"
    ]
    
    try {
        def pipeline
        switch(pipelineConfig.serviceType) {
            case 'frontend':
                pipeline = new FrontendPipeline(scriptContext, pipelineConfig)
                break
            case 'backend':
                pipeline = new BackendPipeline(scriptContext, pipelineConfig)
                break
            default:
                error "Unsupported service type: ${pipelineConfig.serviceType}"
        }
        
        pipeline.execute()
    } catch (Exception e) {
        echo "Pipeline failed: ${e.message}"
        throw e
    }
}