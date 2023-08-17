// The bot comes programmed with preset responses that are said based on the trigger word/phrase
public class TriggerResponsePair
{
    int topicId; // Each pair is linked to a topic
    int usefulness; // A score indicating whether a score is useful or not. The higher it is, the more likely it will be picked
    String trigger; // The word or whole phrase that triggers a response
    String response; // The response itself
    boolean matchFullTrigger; // Whether the whole phrase must match or only part of it
    boolean matchFacts; // Whether to match triggers to the user's immediate answer or a list of previous answers (AKA facts)
}
