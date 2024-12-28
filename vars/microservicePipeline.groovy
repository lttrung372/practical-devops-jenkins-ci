import org.company.template.FrontendPipeline
import org.company.template.BackendPipeline

def call(Map config) {
    def pipeline
    
    switch(config.serviceType) {
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