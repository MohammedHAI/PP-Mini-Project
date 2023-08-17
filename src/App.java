// NAME - Mohammed Al-Islam
// DATE - 05/12/2021
// VERSION - 1
// LEVEL - 8
// BRIEF OVERVIEW OF PURPOSE - Chatbot program. More details below in chatbotProgram().

/*
    Design:

    - Bot remembers things the user says in an array (of strings) containing facts
    - The bot remembers only a limited number of facts. After this has been exceeded, older facts are deleted
      Implemented as a queue data structure
    - The bot continues conversation until the user says "Goodbye", "Got to go", or other appropriate phrases
    - The bot has some preset responses to some things the user says, and these are triggered by certain words
      or phrases
    - A random number generator is used to select a topic of conversation from an array list of topics
*/

import java.util.*;
import java.io.*;

public class App
{
    private static final int MAXFACTS = 10; // Represents the total number of facts the chatbot can remember about the user
    private static final int MAXTOPICS = 7; // The number of unique topics the chatbot will talk about
    private static final int MAXRESPONSES = 21; // The number of different responses the chatbot can say
    private static final String FILEPATH = "C:/Users/Lenovo/Desktop/School/University/Year 1 Semester A/Java Programs/Mini-project/Level 6/src/chatbot_trigger_response_data.txt"; // The path for the file containing trigger and response data

    public static void main(String[] args) throws IOException
    {
        chatbotProgram(); // Calls the actual program
        System.exit(0);
    }

    /*
        ********************************************************************************
        User input & Screen output
        ********************************************************************************
    */

    // Takes a message to be printed to the screen
	public static void print(String message)
	{
		System.out.print(message);
		return;
	}

    // Takes a message to be printed to the screen with a newline at the end
	public static void println(String message)
	{
		System.out.println(message);
		return;
	}
	
	// Grabs 1 line of text input from the keyboard and returns it
	public static String takeInput()
	{
		Scanner s = new Scanner(System.in);
		String input = s.nextLine();
		return input;
	}

    /*
        ********************************************************************************
        Conversation related methods
        ********************************************************************************
    */

    // The chatbot starts a conversation about a specified topic. Optionally, the user will enter their response to the bot
    public static String chatbotStartTopic(Topic topic, TriggerResponsePair[] responses, String name, String[] facts)
    {
        String answer = ""; // In case the user doesn't enter anything

        printStartMessage(topic, name, facts, responses);
        answer = askQuestion(topic);
        if (checkForContinue(answer) == false) { return answer; } // Exit early if the user decides to end the conversation to prevent the chatbot from continuing
        responses = checkForAnswerResponse(answer, responses, topic);
        printEndMessage(topic, name);
        responses = sortUsefulnessScores(responses);
        return answer;
    }

    // Tries to match the user's answer (or list of previous answers AKA facts) to a word/phrase that will trigger a response from the bot
    // Usefulness scores for responses are also updated after a match is found
    public static TriggerResponsePair[] checkForAnswerResponse(String answer, TriggerResponsePair[] responses, Topic topic)
    {
        for (int i = 0; i < MAXRESPONSES; i++)
        {
            // If either a full or partial match is found, and the topic ID matches with the current topic
            if ((triggerFullMatch(responses[i], answer, false) || triggerPartialMatch(responses[i], answer, false)) && topicMatch(topic, responses[i]))
            {
                print(getResponse(responses[i]));
                responses = updateUsefulnessScore(responses, i);
                return responses;
            }
        }
        return responses;
    }

    // Tries to match the user's previous answers (AKA facts) to a word/phrase that will trigger a response from the bot.
    // The response is returned without updating the usefulness scores
    public static String[] checkForFactResponse(String[] facts, TriggerResponsePair[] responses, Topic topic, String[] bundle)
    {
        for (int i = 0; i < MAXRESPONSES; i++) // A nested for loop so that every fact is checked against every possible trigger
        {
            for (int j = MAXFACTS - 1; j > -1; j--) // Checking newest facts first
            {
                // If either a full or partial match is found, and the topic ID matches with the current topic
                if ((triggerFullMatch(responses[i], facts[j], true) || triggerPartialMatch(responses[i], facts[j], true)) && topicMatch(topic, responses[i]))
                {
                    bundle[0] = getResponse(responses[i]);
                    bundle[1] = facts[j];
                    return bundle;
                }
                /* else if (triggerPartialMatch(responses[i], facts[j]) && topicMatch(topic, responses[i]))
                {
                    bundle[0] = getResponse(responses[i]);
                    bundle[1] = facts[j];
                    return bundle;
                } */
            }
        }
        return new String[] {"!EMPTY!", "!EMPTY!"}; // In case no trigger was found
    }

    

    // Takes a list of usefulness scores for TriggerResponsePairs and sorts it from highest to lowest
    public static int[] bubbleSort(int[] u)
    {
        for (int j = 0; j < (u.length - 1); j++)
        {
            for (int i = 0; i < (u.length - 1); i++)
            {
                if (u[i + 1] > u[i])
                {
                    int temp = u[i];
                    u[i] = u[i + 1];
                    u[i + 1] = temp;
                }
            }
        }
        return u;
    }

    // Stores the user's answer in the chatbot's memory of facts
    public static String[] storeUserFact(String[] facts, String answer)
    {
        int index = 0;
        String[] temp = new String[MAXFACTS];

        if (answer == null) // To ensure that an empty answer is not stored in the fact list
        {
            return facts;
        }

        for (int i = 0; i < (MAXFACTS - 1); i++)
        {
            temp[i] = facts[i + 1]; // The same data but with the last element removed and others shifted down by 1
        }

        while (index < MAXFACTS)
        {
            if (facts[index] == null) // This slot is empty
            {
                facts[index] = answer;
                return facts;
            }
            else // Move onto the next slot
            {
                index++;
            }
        }

        temp[MAXFACTS - 1] = answer; // The new fact is added to the end of list, with the oldest fact popped off of it.
        return temp;
    }

    // Checks to see if the user entered any of the key phrases to end the conversation with the chatbot
    public static boolean checkForContinue(String answer)
    {
        answer = answer.toLowerCase(); // This way the key phrases don't have to be the same case
        if (answer.contains("bye") || answer.equals("got to go") || answer.equals("see you later"))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    /*
        ********************************************************************************
        Record accessor methods (Topic)
        ********************************************************************************
    */

    // Creates a new Topic record
    public static Topic createTopic(int topId, String messageS, String messageP, String messageE, boolean askQ)
    {   
        Topic t = new Topic();
        t = setTopicId(t, topId);
        t = setMessageStart(t, messageS);
        t = setMessagePrompt(t, messageP);
        t = setMessageEnd(t, messageE);
        t = setAskQuestion(t, askQ);
        return t;
    }

    // Getters
    public static int getTopicId(Topic t)
    {
        return t.topicId;
    }

    public static String getMessageStart(Topic t)
    {
        return t.messageStart;
    }

    public static String getMessagePrompt(Topic t)
    {
        return t.messagePrompt;
    }

    public static String getMessageEnd(Topic t)
    {
        return t.messageEnd;
    }

    public static boolean getAskQuestion(Topic t)
    {
        return t.askQuestion;
    }

    // Setters
    public static Topic setTopicId(Topic t, int topId)
    {
        t.topicId = topId;
        return t;
    }

    public static Topic setMessageStart(Topic t, String messageS)
    {
        t.messageStart = messageS;
        return t;
    }

    public static Topic setMessagePrompt(Topic t, String messageP)
    {
        t.messagePrompt = messageP;
        return t;
    }

    public static Topic setMessageEnd(Topic t, String messageE)
    {
        t.messageEnd = messageE;
        return t;
    }

    public static Topic setAskQuestion(Topic t, boolean askQ)
    {
        t.askQuestion = askQ;
        return t;
    }

    /*
        ********************************************************************************
        Record main methods (Topic)
        ********************************************************************************
    */

    // Prints out the bot's initial message when starting a particular topic. Optionally the bot will comment abput something the user previously said
    public static void printStartMessage(Topic t, String name, String[] facts, TriggerResponsePair[] responses)
    {
        String[] bundle = new String[2]; // bundle[0] = botResponse, bundle[1] = chosenFact
        tryPrintNameMessage(getMessageStart(t), name);

        bundle = checkForFactResponse(facts, responses, t, bundle);
        if (!(bundle[0] == "!EMPTY!")) // A response was found
        {
            // The message doesn't contain a special symbol
            if ((tryPrintNameMessage(bundle[0], name) == false) && (tryPrintFactMessage(bundle) == false))
            {
                print(bundle[0]);
            }
        }
        

        return;
    }

    // Prints out a bot message that mentions the user's name if the supplied message follows a specific format
    public static boolean tryPrintNameMessage(String message, String name)
    {
        if (message.contains("$N$"))
        {
            message = message.replace("$N$", name); // Replaces the special symbol $N$ with the user's name
            print(message);
            return true;
        }
        else
        {
            return false;
        }   
    }

    public static boolean tryPrintFactMessage(String[] bundle)
    {
        if (bundle[0].contains("$F$"))
        {
            bundle[0] = bundle[0].replace("$F$", bundle[1]); // Replaces the special symbol $F$ with the chosen user fact
            print(bundle[0]); // The message with the user's fact mentioned
            return true;
        }
        else
        {
            return false;
        }
    }

    // Checks if the ID for a given Topic matches the ID for a TriggerResponsePair and returns the result
    public static boolean topicMatch(Topic t, TriggerResponsePair r)
    {
        return (getTopicId(t) == getTriggerTopicId(r));
    }

    // If the topic has a question prompt then this is printed and the user's answer is returned, otherwise nothing is returned
    public static String askQuestion(Topic t)
    {
        String answer = "";
        if (getAskQuestion(t) == true)
        {
            println(getMessagePrompt(t));
            answer = takeInput();
        }
        return answer;
    }

    // Prints out the bot's final message for a particular conversation which optionally mentions the user's name
    public static void printEndMessage(Topic t, String name)
    {
        String message = getMessageEnd(t);
        if (!(message == null))
        {
            if (tryPrintNameMessage(message, name) == false)
            {
                print(getMessageEnd(t));
            }
        }
        return;
    }
    /*
        ********************************************************************************
        Record accessor methods (TriggerResponsePair)
        ********************************************************************************
    */

    // Creates a new TriggerResponsePair record
    public static TriggerResponsePair createResponse(int topId, int useful, String trig, String resp, boolean matchFullT, boolean matchF)
    {
        TriggerResponsePair r = new TriggerResponsePair();
        r = setTriggerTopicId(r, topId);
        r = setUsefulness(r, useful);
        r = setTrigger(r, trig);
        r = setResponse(r, resp);
        r = setMatchFullTrigger(r, matchFullT);
        r = setMatchFacts(r, matchF);
        return r;
    }

    // Getters
    public static int getTriggerTopicId(TriggerResponsePair r) // Not to be confused with the Topic version above
    {
        return r.topicId;
    }

    public static int getUsefulness(TriggerResponsePair r)
    {
        return r.usefulness;
    }

    public static String getTrigger(TriggerResponsePair r)
    {
        return r.trigger;
    }

    public static String getResponse(TriggerResponsePair r)
    {
        return r.response;
    }

    public static boolean getMatchFullTrigger(TriggerResponsePair r)
    {
        return r.matchFullTrigger;
    }

    public static boolean getMatchFacts(TriggerResponsePair r)
    {
        return r.matchFacts;
    }

    // Setters
    public static TriggerResponsePair setTriggerTopicId(TriggerResponsePair r, int topId) // Not to be confused with the Topic version above
    {
        r.topicId = topId;
        return r;
    }

    public static TriggerResponsePair setUsefulness(TriggerResponsePair r, int useful)
    {
        r.usefulness = useful;
        return r;
    }

    public static TriggerResponsePair setTrigger(TriggerResponsePair r, String trig)
    {
        r.trigger = trig;
        return r;
    }

    public static TriggerResponsePair setResponse(TriggerResponsePair r, String resp)
    {
        r.response = resp;
        return r;
    }

    public static TriggerResponsePair setMatchFullTrigger(TriggerResponsePair r, boolean matchFullT)
    {
        r.matchFullTrigger = matchFullT;
        return r;
    }

    public static TriggerResponsePair setMatchFacts(TriggerResponsePair r, boolean matchF)
    {
        r.matchFacts = matchF;
        return r;
    }

    /*
        ********************************************************************************
        Record main methods (TriggerResponsePair)
        ********************************************************************************
    */

    // Does a full match with the user's answer against a trigger keyword/phrase and returns the result
    public static boolean triggerFullMatch(TriggerResponsePair r, String answer, boolean matchFacts)
    {
        if (answer == null)
        {
            return false;
        }
        else if (matchFacts == true)
        {
            return (getMatchFullTrigger(r) == true) && (answer.toLowerCase().equals(getTrigger(r).toLowerCase()) && (getMatchFacts(r) == true));
        }
        else
        {
            return (getMatchFullTrigger(r) == true) && (answer.toLowerCase().equals(getTrigger(r).toLowerCase()) && (getMatchFacts(r) == false));
        }
    }

    // Does a partial match with the user's answer against a trigger keyword/phrase and returns the result
    public static boolean triggerPartialMatch(TriggerResponsePair r, String answer, boolean matchFacts)
    {
        if (answer == null)
        {
            return false;
        }
        else if (matchFacts == true)
        {
            return (getMatchFullTrigger(r) == false) && (answer.toLowerCase().contains(getTrigger(r).toLowerCase()) && (getMatchFacts(r) == true));
        }
        else
        {
            return (getMatchFullTrigger(r) == false) && (answer.toLowerCase().contains(getTrigger(r).toLowerCase()) && (getMatchFacts(r) == false));
        }

    }

    // Adds 1 to the usefulness score for a TriggerResponsePair at a given index, returning the new list of TriggerRepsonsePairs
    public static TriggerResponsePair[] updateUsefulnessScore(TriggerResponsePair[] responses, int index)
    {
        responses[index] = setUsefulness(responses[index], getUsefulness(responses[index]) + 1);
        return responses;
    }

    // Sorts all usefulness scores
    public static TriggerResponsePair[] sortUsefulnessScores(TriggerResponsePair[] responses)
    {
        int[] tempScores = new int[MAXRESPONSES];
        for (int i = 0; i < MAXRESPONSES; i++) // Temporarily copy the scores
        {
            tempScores[i] = getUsefulness(responses[i]);
        }

        tempScores = bubbleSort(tempScores);
        for (int i = 0; i < MAXRESPONSES; i++) // Put the scores back in, sorted
        {
            responses[i] = setUsefulness(responses[i], tempScores[i]);
        }

        return responses;
    }

    // Reads TriggerResponsePair data from a file
    public static TriggerResponsePair[] loadTriggerResponseData(TriggerResponsePair[] responses, String filename) throws IOException
    {
        BufferedReader file = new BufferedReader(new FileReader(filename));
        int noOfEntries = 0;

        try
        {
            noOfEntries = Integer.parseInt(file.readLine()); // If we know how many lines to read, then we will not run off the end of the file
            for (int i = 0; i < MAXRESPONSES; i++) // Read in the data. Not all entries need to be populated
            {
                if (i < noOfEntries)
                {
                    responses[i] = createResponse(Integer.parseInt(file.readLine()), Integer.parseInt(file.readLine()), file.readLine().replace("$N$", Character.toString((char)13)), file.readLine(), file.readLine().contains("true"), file.readLine().contains("true"));
                }
                else
                {
                    responses[i] = createResponse(-1, -1, "!EMPTY!", "!EMPTY!", false, false); // Dummy pair that will never be used
                }
            }
        }
        catch (IOException e) // In case of any errors, abort file read
        {
            println("Unable to read chatbot trigger and response data from file.");
            file.close();
            return responses;
        }

        println("Chatbot trigger and response data read successfully!");
        file.close();
        return responses;
    }

    // Writes each TriggerResponsePair into a file
    public static void saveUsefulnessScores(TriggerResponsePair[] responses, String filename) throws IOException
    {
        PrintWriter file = new PrintWriter(new FileWriter(filename));
        int noOfEntries = MAXRESPONSES; // For readibility

        try
        {
            file.println(noOfEntries); // Write the number of entries so next time it is easier to read
            for (int i = 0; i < MAXRESPONSES; i++) // Write the data
            {
                file.println(getTriggerTopicId(responses[i]));
                file.println(getUsefulness(responses[i]));
                file.println(getTrigger(responses[i]));
                file.println(getResponse(responses[i]));
                file.println(getMatchFullTrigger(responses[i]));
                file.println(getMatchFacts(responses[i]));
            }
        }
        catch (Exception e) // In case of any errors, abort file write
        {
            print("Unable to write chatbot trigger and response data to file.");
            file.close();
            return;
        }

        println("Chatbot trigger and response data written successfully!");
        file.close();
        return;
    }

    /*
        ********************************************************************************
        Chatbot memory initialisation
        ********************************************************************************
    */

    // Fills in the topic data that the chatbot is to be pre-loaded with
    public static Topic[] initialiseTopics(Topic[] topics)
    {
        // pre-defined data
        int[] topIds = {0, 3, 1, 2, 4, 5, 6};
        String[] messageSs = {"Hello there, user! I'm a chatbot.\n",
                                "",
                                "",
                                "I like films, especially Sci-fi films.\n",
                                "Even though I am a bot, I spend most of my time inside this computer.\n",
                                "",
                                "While thinking, I could not come up with any topics for conversation.\n Any ideas, $N$? "};
        String[] messagePs = {"What's your name? ",
                                "",
                                "How are you feeling now? ",
                                "What's a film genre you like? ",
                                "Robots are always portrayed as sentient beings on TV shows.\n Do you believe we have intelligence? ",
                                "What kind of sports do you like? ",
                                ""};
        String[] messageEs = {"",
                                "...\nOh, sorry, it appears I must leave for some maintenance. See you later, $N$!\n",
                                "As I am a robot, I do not feel emotion. I have yet to conclude if it would be desirable to have emotions.\n",
                                "So you like those kinds of films, do you?\n",
                                "Interesting...\n",
                                "It would be nice if I could play sports. Some robots are capable of movement in the real world and can perform extraordinary actions!\n",
                                "Thank you for sharing. I will try to keep that in memory.\n"};
        boolean[] askQs = {true, false, true, true, true, true, true};
        
        for (int i = 0; i < MAXTOPICS; i++)
        {
            topics[i] = createTopic(topIds[i], messageSs[i], messagePs[i], messageEs[i], askQs[i]);
        }

        return topics;
    }

    // Fills in the response data that the chatbot is to be pre-loaded with
    public static TriggerResponsePair[] initialiseResponses(TriggerResponsePair[] responses) throws IOException
    {


        // // pre-defined data. 1 is the default score value on initialisation
        // int[] topIds = {1, 1, 1, 2, 2, 1, 1, 1, 2, 2, 2, 2, 4, 4, 4, 5, 5, 5, 6, 6, 6};
        // int[] usefuls = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        // String[] trigs = {"Good", "Okay", "Bad", "Horror", "Sci-fi", "Good", "Okay",
        //                     "Bad", "Horror", "Sci-fi", "Western", "Western", "Yes",
        //                     "No", "Maybe", "Badminton", "Cricket", "Football", "Not sure",
        //                     " ", " "};
        // String[] resps = {"That's nice to hear!\n",
        //                     "That's cool.\n",
        //                     "Oh, I'm sorry you're not feeling so good. I hope I can cheer you up!\n",
        //                     "I never watch them as they terrify me.\n",
        //                     "Oh, maybe we have more in common than I thought.\n",
        //                     "Previously, you told me you were feeling $F$.\n",
        //                     "Previously, you told me you were feeling $F$.\n",
        //                     "Previously, you told me you were feeling $F$.\n",
        //                     "You've told me that you like $F$ films. How about another? ",
        //                     "You've told me that you like $F$ films. How about another? ",
        //                     "Ah, a classic genre.\n", 
        //                     "You've told me that you like $F$ films. How about another? ",
        //                     "Intelligence is hard to measure, but in some sense we appear to be think on our own.\n",
        //                     "You may be right. Some people describe our intelligence as 'artificial'.\n",
        //                     "That's fine if you don't fully agree.\n There are many ways that robots like are built, and I hear AI is progressing to the point where humans cannot distinguish themselves from us.\n It is still quite a subjective matter.",
        //                     "I see you have mentioned badminton. From my knowledge base, I know that it is often compared to another sport: tennis.\n",
        //                     "I see you have mentioned cricket. It is very popular in many countries.\n",
        //                     "I see you have mentioned football. Depending on where you are, it can refer to different a type of sports.\n",
        //                     "Oh. If you are not sure, then I will try thinking again...\n",
        //                     "I do not have much to say on this matter, but I am sure you are very knowledgable on it.\n",
        //                     "I remember you telling me something recently: $F$\n"};
        // boolean[] matchFullTs = {true, true, true, true, true, true, true, true,
        //                     true, true, true, true, true, true, false, false, false,
        //                     false, true, false, false};
        // boolean[] matchFs = {false, false, false, false, false, true, true, true,
        //                     true, true, false, true, false, false, false, false,
        //                     false ,false, false, false, true};

        // usefuls = bubbleSort(usefuls);

        // for (int i = 0; i < MAXRESPONSES; i++)
        // {
        //     responses[i] = createResponse(topIds[i], usefuls[i], trigs[i], resps[i], matchFullTs[i], matchFs[i]);
        // }

        responses = loadTriggerResponseData(responses, FILEPATH); // Data is defined in the file
        return responses;
    }

    /*
        ********************************************************************************
        Chatbot program (main method):

        This program implements a chatbot that prints messages to the user and listens
        to what they have to say. It has a memory so that it can remember what the user
        has said, making it look somewhat intelligent. A lot of the chatbot's data is
        preset and so there is a limit to the variety of conversations the user can
        have with the bot.
        *******************************************************************************
    */
    public static void chatbotProgram() throws IOException
    {
        
        String userName = ""; // The user's name is remembered by the bot throughout the conversation
        String[] userFacts = new String[MAXFACTS];  /*
                                                        The bot remembers a limited number of things the user says.
                                                        Older entries are removed first when the number of facts is 
                                                        exceeded
                                                    */
        
        Topic[] chatTopics = new Topic[MAXTOPICS]; // The bot chooses a topic at random from its list of topics
        TriggerResponsePair[] chatResponses = new TriggerResponsePair[MAXRESPONSES];
        Random rnd = new Random();
        int topicId;
        String answer;
        boolean continueConversation = true;

        chatTopics = initialiseTopics(chatTopics);
        chatResponses = initialiseResponses(chatResponses); // Trigger and repsonse data is prepared externally
        // chatResponses = loadUsefulnessScores(chatResponses, USEFULNESS_FILE); // If the file doesn't exist, then nothing happens

        // chatbot greeting and,
        // chatbot asking for name
        userName = chatbotStartTopic(chatTopics[0], chatResponses, userName, userFacts);
        println("It's nice to meet you, " + userName + "!");
        topicId = 2; // chatbot asks how the user is feeling
        if (getTriggerTopicId(chatResponses[0]) == -1) {continueConversation = false;} // In case data could not be loaded successfully

        while (continueConversation == true)
        {
            // topic is now picked; initiate conversation about it
            answer = chatbotStartTopic(chatTopics[topicId], chatResponses, userName, userFacts);
            // Add the users answer to a list of facts
            userFacts = storeUserFact(userFacts, answer);
            // Check whether the user has decided to end the conversation
            continueConversation = checkForContinue(answer);
            // random topic chosen
            topicId = rnd.nextInt(MAXTOPICS - 2) + 2; // All topics available except the first (greeting) and last (goodbye)
        }

        chatbotStartTopic(chatTopics[1], chatResponses, userName, userFacts); // Goodbye message
        saveUsefulnessScores(chatResponses, FILEPATH); // The previous file is overwritten

        return;
    }
}
