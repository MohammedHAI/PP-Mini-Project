// Chat topics. All the possible conversations the chatbot is capable of having
public class Topic
{
    int topicId;
    String messageStart; // The message to be displayed at the start of the new topic
    String messagePrompt; // The message to be displayed when prompting the user for input
    String messageEnd; // The message to be displayed when ending the current topic
    boolean askQuestion; /* Whether the chatbot will ask the user a question or not. If yes,
                            then the input prompt is taken from messagePrompt */
}