package com.wtf.app.parent;

import com.wtf.app.commons.InitialSetup;

import com.wtf.app.model.enums.SocialType;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.model.enums.TaskSocialData;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.function.Supplier;

import com.wtf.app.util.ThreadLocalAutomationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("constants")
public abstract class Constants {

    public static final String CONSULTANTS_PHONE_NUMBERS_FILE = "consultantsPhoneNumbers.txt";
    public static final int MINIMUM_EXPORT_COUNT_INTERVAL = 50;
    public static final int DURATION_DRIVER_SETUP_AND_LOADING_AFTER_SCANNED = 60000;//120000
    public static final int _ERROR_GROUP_PROCESS_SLEEP_TIME = 7000;
    public static final int GROUP_EXECUTION_INTERVAL_MS = 6000;
    public static final Map<TaskType, TaskSocialData> TASK_SOCIAL_DATA = new ConcurrentHashMap<>();

    // Log4j2 logger
    protected static final Logger logger = LogManager.getLogger(Constants.class);
    public static final String DB_FILES = "dbFiles/";

    @Value("${daysToSubtract:2}")
    private static int daysToSubtract;

    protected final InitialSetup initialSetup;

    // Static fields
    public int counter = 0;
    public static Boolean yesReprocessErrorList = false;

    @Autowired
    public Constants( InitialSetup initialSetup) {

        this.initialSetup = initialSetup;
        addFixedTaskSpecificData();
    }

    public static String getCurrentDate() {
        return LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("ddMMyyyy-HH:mm"));
    }

    protected static String ERROR_GROUPS_TXT = DB_FILES+"errorGroups.txt";
    protected static String ALL_SUPPORT_GROUPS = DB_FILES+"successfulGroups.txt";
    protected Set<String> allSuccessfulSupportGroupsFromFile = extractListOfDataFromFile(ALL_SUPPORT_GROUPS);

    protected String EXCLUDE_PERSONAL_GROUP_NAMES_FROM_CSV = DB_FILES+"master_fixed_files/exclude_personal_group_names_always.txt";
    protected final Set<String> excludePersonalGroupNamesSet = extractListOfDataFromFile(EXCLUDE_PERSONAL_GROUP_NAMES_FROM_CSV);

    protected String EXCLUDE_SUPPORT_GROUP_NAMES_FROM_CSV = DB_FILES+"master_fixed_files/exclude_group_support_names_always.txt";
    protected final Set<String> excludeSupportGroupNamesSet = extractListOfDataFromFile(EXCLUDE_SUPPORT_GROUP_NAMES_FROM_CSV);

    protected String EXCLUDE_PERSONAL_GROUP_CONTACT_NAMES_FROM_CSV = DB_FILES+"master_fixed_files/exclude_group_contact_names_always_for_the_day.txt";//to avoid newly added/created personal group or newly joined members
    protected Set<String> excludePersonalGroupContactNamesOrNumbersSet = extractListOfDataFromFile(EXCLUDE_PERSONAL_GROUP_CONTACT_NAMES_FROM_CSV);

    protected static String NEGLECT_TO_JOIN_GROUPS_WITH_TEXT = DB_FILES+"master_fixed_files/neglects_group_names_text_to_join_always.txt";
    private static Set<String> neglectToJoinGroupsWithTextSet = extractListOfDataFromFile(NEGLECT_TO_JOIN_GROUPS_WITH_TEXT);
    protected final Set<String> neglectToJoinGroupsWithTextSetLowerCase = neglectToJoinGroupsWithTextSet.parallelStream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
            
    public static String getNEGLECT_TO_JOIN_GROUPS_TXT() {
        return NEGLECT_TO_JOIN_GROUPS_WITH_TEXT;
    }
    
    public static void setNEGLECT_TO_JOIN_GROUPS_TXT(String neglectToJoinGroupsTxt) {
        NEGLECT_TO_JOIN_GROUPS_WITH_TEXT = neglectToJoinGroupsTxt;
        // Update the set when the filename changes
        neglectToJoinGroupsWithTextSet = extractListOfDataFromFile(NEGLECT_TO_JOIN_GROUPS_WITH_TEXT);
    }

    protected String BROADCAST_GROUPS = DB_FILES+"watg_group_names.txt";
    protected Set<String> broadcastGroupsSet = extractListOfDataFromFile(BROADCAST_GROUPS);
    public static final String GROUP_STRING_TRAVERSE_ALREADY_DATE_WATG = "group_traverse_already_date_";
    protected static String GROUP_NAMES_SPECIAL_CHAR_TO_SPLIT_EXTRACTOR_CLASS_REF_TXT = "group_names_specialChar_to_split_ExtractorClassRef.txt";

    private static String WHATSAPP_ALREADY_JOINED_GROUP_NAMES_STR = DB_FILES+"watg_group_join_links_already_joined_whatsapp.txt";
    protected static Set<String> whatsappAlreadyJoinedGroupsSet = extractListOfDataFromFile(WHATSAPP_ALREADY_JOINED_GROUP_NAMES_STR);

    private static String TELEGRAM_ALREADY_JOINED_GROUP_NAMES_STR = DB_FILES+"watg_group_join_links_already_joined_telegram.txt";
    protected static Set<String> telegramAlreadyJoinedGroupsSet = extractListOfDataFromFile(TELEGRAM_ALREADY_JOINED_GROUP_NAMES_STR);

    private static String WHATSAPP_BROADCAST_GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT = DB_FILES+"watg_groups_already_message_sent_for_the_day_whatsapp.txt";
    protected static Set<String> whatsappBroadcastGroupsAlreadyMessageSentForTheDaySet = extractListOfDataFromFile(WHATSAPP_BROADCAST_GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT);

    /* Not required as skipping as single users. for future use case*/
    private static String WHATSAPP_TRAVERSE_GROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT = DB_FILES+"watg_groups_and_admins_already_traversed_for_the_day_whatsapp.txt";
    protected static Set<String> whatsappTraverseGroupsAdminsAlreadyTraversedForTheDaySet = extractListOfDataFromFile(WHATSAPP_TRAVERSE_GROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT);


    private static String TELEGRAM_BROADCAST_GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT = DB_FILES+"watg_groups_already_message_sent_for_the_day_telegram.txt";
    protected static Set<String> telegramBroadcastGroupsAlreadyMessageSentForTheDaySet = extractListOfDataFromFile(TELEGRAM_BROADCAST_GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT);

    /* Not required as skipping as single users. for future use case*/
    private static String TELEGRAM_TRAVERSE_GROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT = DB_FILES+"watg_groups_and_admins_already_traversed_for_the_day_telegram.txt";
    protected static Set<String> telegramTraverseGroupsAdminsAlreadyTraversedForTheDaySet = extractListOfDataFromFile(TELEGRAM_TRAVERSE_GROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT);


    /* for Facebook */
    private static final String BROADCAST_GROUP_ID_NAMES_FB = DB_FILES+"master_fixed_files/fb_group_id_name_mapping_support_proxy.txt";
    protected static Set<String> broadcastGroupIdNameFBSet = extractListOfDataFromFile(BROADCAST_GROUP_ID_NAMES_FB);

    private static final String BROADCAST_GROUP_ID_PENDING_POST_PILED_GROUPS_FB = DB_FILES+"fb_groups_pending_posts_message_piled.txt";
    protected static Set<String> pendingPostPiledGroupsFB_set = extractListOfDataFromFile(BROADCAST_GROUP_ID_PENDING_POST_PILED_GROUPS_FB);

    private static final String BROADCAST_SENSITIVE_GROUP_ID_NAMES_FB = DB_FILES+"master_fixed_files/fb_sensitive_group_id_name_mapping_training.txt";
    protected static Set<String> broadcastSensitiveGroupIdNameFBSet = extractListOfDataFromFile(BROADCAST_SENSITIVE_GROUP_ID_NAMES_FB);

    protected static final String GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_FB = DB_FILES+"fb_groups_already_message_sent_forTheDay.txt";
    protected static final String GROUPS_PENDING_POSTS_MESSAGE_PILED_UP_FB = DB_FILES+"fb_groups_pending_posts_message_piled.txt";
    protected Set<String> groupsAlreadyMessageSentForTheDaySetFB = extractListOfDataFromFile(GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_FB);
    protected String GROUP_STRING_TRAVERSE_ALREADY_DATE_FB = "group_traverse_already_date_fb_";

    public static final String _TEST_SUITE = "_TEST_SUITE";
    public static final Function<String, String> runningTestSuiteApenderFunction = 
            fileName -> fileName.replace(".", (_TEST_SUITE + "."));
    public final Supplier<String> getLocalTimeInFormat = 
            () -> LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("ddMMyyyy-HH:MM"));
    public final Supplier<String> getLocalDateOnlyInFormat = 
            () -> LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("ddMMyyyy"));



    public static Set<String> extractListOfDataFromFile(String groupNamesFromCsv) {
        Set<String> fetchAllFromFile = null;
        try {
            fetchAllFromFile = getGroupListFromCSV(groupNamesFromCsv)
                    .parallelStream()
                    .collect(Collectors.toSet());
        } catch (Exception ex) {
            logger.error("Exception in reading file extractListOfDataFromFile: " + ex.getLocalizedMessage(), ex);
        }
        return fetchAllFromFile != null ? fetchAllFromFile : Set.of();
    }

    public static Set<String> getGroupListFromCSV(String considerGroupNamesFromCsv) {
        Path path = Paths.get(considerGroupNamesFromCsv);
        Charset charset = StandardCharsets.ISO_8859_1;
        Set<String> csvRecords = new CopyOnWriteArraySet<>();
        String line = null;
        String lastExecutedLine = null;

        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            while ((line = reader.readLine()) != null) {
                lastExecutedLine = line;
                csvRecords.add(line);
            }
        } catch (IOException e) {
            logger.error("Error reading file: " + considerGroupNamesFromCsv + 
                       ", Last Executed Line: " + lastExecutedLine, e);
        }
        logger.debug("getPersonalGroupListFromCSV Completed: {}", considerGroupNamesFromCsv);
        return csvRecords;
    }

    @FunctionalInterface
    public interface CustomFunction {
        Object execute();
    }

    public static boolean containsOnlyPrintableAscii(String text) {
        if (text == null) {
            return false;
        }
        for (char c : text.toCharArray()) {
            if (!(c >= 32 && c <= 126)) {
                logger.error("Junk character found in text: {}", text);
                return false;
            }
        }
        return true;
    }

    public static String removeJunkCharactersIfExists(String text) {
        if (text == null || containsOnlyPrintableAscii(text)) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isLetterOrDigit(c) || Character.isWhitespace(c) ||
                (c >= 32 && c <= 126)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static Optional<Object> runTC(CustomFunction func) {
        try {
            return Optional.ofNullable(func.execute());
        } catch (Exception ex) {
            logger.error("Function execution failed: " + func, ex);
            return Optional.empty();
        }
    }

    public static boolean isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3Days(Set<String> socialWritableList){

        return   /*socialWritableList.contains("group_traverse_already_date_" + LocalDate.now()) ||*/
        IntStream.range(0,daysToSubtract+1).anyMatch(i->socialWritableList
                .contains("group_traverse_already_date_" + LocalDate.now().minusDays(i)))/* ||
                        socialWritableList.contains("group_traverse_already_date_" + LocalDate.now().minusDays(2L))*/;

    }

    public static String getErrorGroupsTxt() {
        return ERROR_GROUPS_TXT;
    }

    public static String getAllSupportGroups() {
        return ALL_SUPPORT_GROUPS;
    }

    public Set<String> getAllSuccessfulSupportGroupsFromFile() {
        return allSuccessfulSupportGroupsFromFile;
    }

    public String getEXCLUDE_PERSONAL_GROUP_NAMES_FROM_CSV() {
        return EXCLUDE_PERSONAL_GROUP_NAMES_FROM_CSV;
    }

    public Set<String> getExcludePersonalGroupNamesSet() {
        return excludePersonalGroupNamesSet;
    }

    public String getEXCLUDE_SUPPORT_GROUP_NAMES_FROM_CSV() {
        return EXCLUDE_SUPPORT_GROUP_NAMES_FROM_CSV;
    }

    public Set<String> getExcludeSupportGroupNamesSet() {
        return excludeSupportGroupNamesSet;
    }

    public String getEXCLUDE_PERSONAL_GROUP_CONTACT_NAMES_FROM_CSV() {
        return EXCLUDE_PERSONAL_GROUP_CONTACT_NAMES_FROM_CSV;
    }

    public Set<String> getExcludePersonalGroupContactNamesOrNumbersSet() {
        return excludePersonalGroupContactNamesOrNumbersSet;
    }

    public static String getNeglectToJoinGroupsWithText() {
        return NEGLECT_TO_JOIN_GROUPS_WITH_TEXT;
    }

    public Set<String> getNeglectToJoinGroupsWithTextSetLowerCase() {
        return neglectToJoinGroupsWithTextSetLowerCase;
    }

    public String getBROADCAST_GROUPS() {
        return BROADCAST_GROUPS;
    }

    public Set<String> getBroadcastGroupsSet() {
        return broadcastGroupsSet;
    }

    public static String getGroupNamesSpecialCharToSplitExtractorClassRefTxt() {
        return GROUP_NAMES_SPECIAL_CHAR_TO_SPLIT_EXTRACTOR_CLASS_REF_TXT;
    }

    public static String getWhatsappAlreadyJoinedGroupNamesStr() {
        return WHATSAPP_ALREADY_JOINED_GROUP_NAMES_STR;
    }

    public Set<String> getWhatsappAlreadyJoinedGroupsSet() {
        return whatsappAlreadyJoinedGroupsSet;
    }

    public static String getTelegramAlreadyJoinedGroupNamesStr() {
        return TELEGRAM_ALREADY_JOINED_GROUP_NAMES_STR;
    }

    public Set<String> getTelegramAlreadyJoinedGroupsSet() {
        return telegramAlreadyJoinedGroupsSet;
    }

    public static String getBroadcastGroupsAlreadyMessageSentForTheDayTxt() {
        return TASK_SOCIAL_DATA.get(ThreadLocalAutomationContext.getContext().getTaskType()).getVisitedGroupFileName();
    }

    public Set<String> getBroadcastGroupsAlreadyMessageSentForTheDaySet() {
        return TASK_SOCIAL_DATA.get(ThreadLocalAutomationContext.getContext().getTaskType()).getVisitedGroupNames();
    }

    public static String getBroadcastGroupsAlreadyMessageSentForTheDayTxt(TaskType taskType) {
        return TASK_SOCIAL_DATA.get(taskType).getVisitedGroupFileName();
    }

    public Set<String> getBroadcastGroupsAlreadyMessageSentForTheDaySet(TaskType taskType) {
        return TASK_SOCIAL_DATA.get(taskType).getVisitedGroupNames();
    }

    public static String getWhatsappTraverseGroupsAndAdminsAlreadyTraversedForTheDayTxt() {
        return WHATSAPP_TRAVERSE_GROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT;
    }

    public Set<String> getWhatsappTraverseGroupsAdminsAlreadyTraversedForTheDaySet() {
        return whatsappTraverseGroupsAdminsAlreadyTraversedForTheDaySet;
    }

    public static String getTelegramBroadcastGroupsAlreadyMessageSentForTheDayTxt() {
        return TELEGRAM_BROADCAST_GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT;
    }

    public Set<String> getTelegramBroadcastGroupsAlreadyMessageSentForTheDaySet() {
        return telegramBroadcastGroupsAlreadyMessageSentForTheDaySet;
    }

    public static String getTelegramTraverseGroupsAndAdminsAlreadyTraversedForTheDayTxt() {
        return TELEGRAM_TRAVERSE_GROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT;
    }

    public Set<String> getTelegramTraverseGroupsAdminsAlreadyTraversedForTheDaySet() {
        return telegramTraverseGroupsAdminsAlreadyTraversedForTheDaySet;
    }

    //overriding in fb subclasses
    public Set<String> getBroadcastGroupIdNameFBSet() {
        return broadcastGroupIdNameFBSet;
    }

    public static Set<String> getPendingPostPiledGroupsFB_set() {
        return pendingPostPiledGroupsFB_set;
    }

    public static Set<String> getBroadcastSensitiveGroupIdNameFBSet() {
        return broadcastSensitiveGroupIdNameFBSet;
    }

    public Set<String> getGroupsAlreadyMessageSentForTheDaySetFB() {
        return groupsAlreadyMessageSentForTheDaySetFB;
    }

    public String getGROUP_STRING_TRAVERSE_ALREADY_DATE_FB() {
        return GROUP_STRING_TRAVERSE_ALREADY_DATE_FB;
    }

    public static Set<String> getNeglectToJoinGroupsWithTextSet() {
        return new HashSet<>(neglectToJoinGroupsWithTextSet);
    }

    public static void setNeglectToJoinGroupsWithTextSet(Set<String> groups) {
        neglectToJoinGroupsWithTextSet = new HashSet<>(groups);
    }

    public static Map<TaskType, TaskSocialData> getTASK_SOCIAL_DATA(){
        return TASK_SOCIAL_DATA.isEmpty()? addFixedTaskSpecificData(): TASK_SOCIAL_DATA;
    }


    public static Map<TaskType, TaskSocialData> addFixedTaskSpecificData() {
        TASK_SOCIAL_DATA.put(TaskType.WHATSAPP_FETCH_CONTACTS,
                new TaskSocialData(TaskType.WHATSAPP_FETCH_CONTACTS,
                        SocialType.WHATSAPP,isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3Days(whatsappTraverseGroupsAdminsAlreadyTraversedForTheDaySet),
                        WHATSAPP_TRAVERSE_GROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT,whatsappTraverseGroupsAdminsAlreadyTraversedForTheDaySet,
                        WHATSAPP_ALREADY_JOINED_GROUP_NAMES_STR, whatsappAlreadyJoinedGroupsSet));
        TASK_SOCIAL_DATA.put(TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS,
                new TaskSocialData(TaskType.WHATSAPP_BROADCAST_AD_TO_GROUPS,
                        SocialType.WHATSAPP,isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3Days(whatsappBroadcastGroupsAlreadyMessageSentForTheDaySet),
                        WHATSAPP_BROADCAST_GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT, whatsappBroadcastGroupsAlreadyMessageSentForTheDaySet,
                        null,null));
        TASK_SOCIAL_DATA.put(TaskType.TELEGRAM_FETCH_CONTACTS,
                new TaskSocialData(TaskType.TELEGRAM_FETCH_CONTACTS,
                        SocialType.TELEGRAM,isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3Days(telegramTraverseGroupsAdminsAlreadyTraversedForTheDaySet),
                        TELEGRAM_TRAVERSE_GROUPS_AND_ADMINS_ALREADY_TRAVERSED_FOR_THE_DAY_TXT, telegramTraverseGroupsAdminsAlreadyTraversedForTheDaySet,
                        TELEGRAM_ALREADY_JOINED_GROUP_NAMES_STR, telegramAlreadyJoinedGroupsSet));
        TASK_SOCIAL_DATA.put(TaskType.TELEGRAM_BROADCAST_AD,
                new TaskSocialData(TaskType.TELEGRAM_BROADCAST_AD,
                        SocialType.TELEGRAM,isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3Days(telegramBroadcastGroupsAlreadyMessageSentForTheDaySet),
                        TELEGRAM_BROADCAST_GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT, telegramBroadcastGroupsAlreadyMessageSentForTheDaySet,
                        null,null));
        return TASK_SOCIAL_DATA;
    }


}
