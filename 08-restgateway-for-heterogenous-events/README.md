# Rest Gateway to produces message to Kafka for heterogenous events

## Define Avro Schema for Events

```
@namespace("com.trivadis.kafkaws")

protocol AlertProtocol {
	record Alert {
		uuid id;
		string message;
		string severity;
		timestamp_ms when;
	}
}
```

```
@namespace("com.trivadis.kafkaws")

protocol NotificationProtocol {
	record Notification {
		long id;
		string message;
	}
}
```

## Define Avro Schema for "Message Bundle"


```
@namespace("com.trivadis.kafkaws")

protocol MessagesProtocol {
	import idl "Alert.avdl";
	import idl "Notification.avdl";

	record Messages {
		array<union{Notification,Alert}> items;
	}
}
```

## Rest Controller for REST Gateway

```java
@RestController
@RequestMapping("/gateway")
public class RestGatewayController {
    
    @Autowired
    private StreamBridge streamBridge;

    @PutMapping(produces = AVRO_JSON, consumes = AVRO_JSON)
    public ResponseEntity<Messages> handleJsonEmailData(@RequestBody final Messages data) {
        System.out.println("Data:" + data);
        System.out.println(data);

        for (Object item : data.getItems()) {
            streamBridge.send("send-binding-out-0", (SpecificRecord) item);
            //kafkaTemplate.send("test-kstream-spring-cloudstream-input-topic", (SpecificRecord) item);
        }

        return ResponseEntity.ok(data);
    }

    @ExceptionHandler(value = AvroTypeException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<ErrorResponse> handleAvroTypeException(AvroTypeException avroTypeException, WebRequest request) {
        System.out.println("Exception handled: " + avroTypeException);
        return null;
    }
}
```

## Define Converter

```java
@Configuration
@EnableWebMvc
public class SpringWebConfig implements WebMvcConfigurer {
    @Override
    public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
        converters.add(0, new AvroJsonHttpMessageConverter());
    }

    @Bean
    public RestTemplate restTemplate(final RestTemplateBuilder builder) {
        final RestTemplate restTemplate = builder.build();
        restTemplate.getMessageConverters().add(0, new AvroJsonHttpMessageConverter());
        return restTemplate;
    }
}
```

## Sample JSON Message

```json
{
   "items":[
      {
         "com.trivadis.kafkaws.Notification":{
            "id":1,
            "message":"Hello"
         }
      },
      {
         "com.trivadis.kafkaws.Alert":{
            "id":"6078cebc-d630-45d8-a882-265128580ba3",
            "message":"Hello",
            "severity":"ERROR",
            "when":1673552991000
         }
      }
   ]
}
```

## Send a message using Curl

```bash
curl -X PUT -H 'Accept:  application/avro-json' -H 'Content-Type:  application/avro-json' -i http://localhost:8081/gateway --data '{"items": [ { "com.trivadis.kafkaws.Notification": {"id": "1", "message": "Hello"}}, { "com.trivadis.kafkaws.Alert": {"id": "6078cebc-d630-45d8-a882-265128580ba3", "message": "Hello", "severity": "ERROR", "when": 1673552991000}}]}
'
```