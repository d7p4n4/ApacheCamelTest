package com.company;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.RestConfiguration;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class GetExample {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder()  {
            @Override
            public void configure() throws Exception {
                from("direct:post")
                        .process(new Processor() {
                            public void process(Exchange exchange) throws Exception {
                                System.out.println("POST method started!");
                                //exchange.getIn().setBody("{ \"name\": \"Izsa\" }");
                                exchange.setProperty(Exchange.CHARSET_NAME, "UTF-8");
                                exchange.getIn().setHeader("CamelHttpMethod", "POST");
                                exchange.getIn().setHeader("Content-Type", "application/json");
                                exchange.getIn().setHeader("accept", "application/json");
                            }
                        })
                        // to the http uri
                        .to("http://google.com")
                        // to the consumer
                        .to("seda:post");
            }
        });

        context.addRoutes(new RouteBuilder()  {
            @Override
            public void configure() throws Exception {
                from("direct:get")
                        .process(new Processor() {
                            public void process(Exchange exchange) throws Exception {
                                System.out.println("GET method started!");
                                exchange.getIn().setHeader("CamelHttpMethod", "GET");
                            }
                        })
                        // to the http uri
                        .to("http://google.com")
                        // to the consumer
                        .to("seda:get");
            }
        });
        context.start();
        ProducerTemplate pt = context.createProducerTemplate();
        // for sending request
        ConsumerTemplate ct = context.createConsumerTemplate();

        pt.sendBody("direct:get", "test");
        System.out.println(ct.receiveBody("seda:get", String.class));

        System.out.println("REST requests done!");
        try {
            Thread.sleep(5 * 60 * 1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        context.stop();
    }
}