# Alexa Routing for Java

This repository contains a set of classes that allow you to use 'Controllers' to build Skills for Amazon Alexa. This is a runnable example that can be deployed to Heroku.

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

The `SpeechRouter` takes care of generating the intent schema and sample utterances to speed up your iterations. Moreover you can define each intent in one place, including utterances, slots and the implementation, so everything stays in sync.

This is how it looks like:

```java
public class ExampleController extends AlexaController {

    @Utterances({
        "what is the sum of {one;two;three;four;five|First} and {one;two;three;four;five|Second}",
        "what is {one;two;three;four;five|First} plus {one;two;three;four;five|Second}"
    })
    // This maps the slots from the utterances to the method parameters (so the order is important)
    @Slot({"First", "Second"}) 
    public AlexaResponse addTwoNumbers(int first, int second) {
        return endSessionResponse(String.format("The sum of %1$d and %2$d is %3$d.", 
            first, second, first + second));
    }
}
```

For a more complete example please have a look at the `RottenTomatoesController` class (this even supports Rails-like Action Filters).

**Sample Deployment**

https://alexa-routing-example.herokuapp.com/

- Skill Endpoint: [https://alexa-routing-example.herokuapp.com/rotten-tomatoes](https://alexa-routing-example.herokuapp.com/rotten-tomatoes)
- Sample Utterances: [https://alexa-routing-example.herokuapp.com/sample-utterances](https://alexa-routing-example.herokuapp.com/sample-utterances)
- Intent Schema: [https://alexa-routing-example.herokuapp.com/intent-schema](https://alexa-routing-example.herokuapp.com/intent-schema)
