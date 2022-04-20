package pubsub.springbatch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.AckMode;
import org.springframework.cloud.gcp.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.cloud.gcp.pubsub.support.GcpPubSubHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;
import pubsub.springbatch.listener.JobCompletionNotificationListener;
import pubsub.springbatch.model.entity.OrderEntity;
import pubsub.springbatch.model.pubsub.Order;
import pubsub.springbatch.processor.OrderProcessor;
import pubsub.springbatch.reader.PubSubMessageReader;
import pubsub.springbatch.writer.OrderEntityWriter;

@Configuration
@EnableIntegration
@Slf4j
public class BatchConfiguration {

    @Value("${pubsub.subscription}")
    private String subscription;

    private JobBuilderFactory jobBuilderFactory;

    private StepBuilderFactory stepBuilderFactory;

    private JobLauncher jobLauncher;

    private JobCompletionNotificationListener listener;

    private OrderEntityWriter writer;

    private OrderProcessor processor;

    private ObjectMapper objectMapper;

    public BatchConfiguration(
            final JobBuilderFactory jobBuilderFactory,
            final StepBuilderFactory stepBuilderFactory,
            final JobLauncher jobLauncher,
            final JobCompletionNotificationListener listener,
            final OrderEntityWriter writer,
            final OrderProcessor processor
    ) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobLauncher = jobLauncher;
        this.listener = listener;
        this.writer = writer;
        this.processor = processor;
        this.objectMapper = new ObjectMapper();
    }

    @Bean
    @StepScope
    public ItemReader<Order> pubSubMessageReader(@Value("#{jobParameters[pubSubRawMessage]}") String rawMessage) {
        return new PubSubMessageReader(rawMessage, objectMapper);
    }

    @Bean
    public PubSubInboundChannelAdapter messageChannelAdapter(
            final MessageChannel pubSubInputChannel,
            final PubSubTemplate pubSubTemplate
    ) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, subscription);
        adapter.setOutputChannel(pubSubInputChannel);
        adapter.setAckMode(AckMode.MANUAL);

        return adapter;
    }

    @Bean
    public MessageChannel pubSubInputChannel() {
        return new PublishSubscribeChannel();
    }

    @ServiceActivator(inputChannel = "pubSubInputChannel")
    public void messageReceiver(
            final String pubSubRawMessage,
            final @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message
    ) throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException,
            JobRestartException
    {
        log.debug("Order arrived: {}", pubSubRawMessage);
        message.ack();

        final JobParameters jobParameters = new JobParametersBuilder()
                .addString("pubSubRawMessage", pubSubRawMessage).toJobParameters();

        final Step step = getStep(pubSubRawMessage);

        final Job job = this.jobBuilderFactory.get("processOrderJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(step)
                .build();

        jobLauncher.run(job, jobParameters);
    }

    private TaskletStep getStep(String pubSubRawMessage) {
        return this.stepBuilderFactory.get("step1")
                .<Order, OrderEntity>chunk(1)
                .reader(pubSubMessageReader(pubSubRawMessage))
                .processor(processor)
                .writer(writer)
                .build();
    }
}
