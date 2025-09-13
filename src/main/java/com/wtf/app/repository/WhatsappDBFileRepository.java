package com.wtf.app.repository;

import com.wtf.app.commons.InitialSetup;

import com.wtf.app.model.dto.AutomationContext;
import com.wtf.app.model.enums.SocialType;
import com.wtf.app.parent.Constants;
import com.wtf.app.util.ThreadLocalAutomationContext;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import jakarta.inject.Provider;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Repository
@Lazy
public class WhatsappDBFileRepository extends Constants {

    public static final Predicate<String> CONTACT_PREDICATE = str ->
        str != null && !str.isEmpty() && str.length() > 1 &&
        !Character.isLetter(str.charAt(0)) &&
        !Character.isLetter(str.charAt(1)) &&
        str.length() < 50;

    private static final String SUPPORT_EXTRACTED_CONTACTS = "D:\\SupportDetails\\Support_ExtractSupportProxyContacts\\contacts";
    private static final Charset CHARSET = StandardCharsets.UTF_8;  // Using UTF-8 consistently

	private final ParallelWebDriverManager parallelWebDriverManager;

	@Autowired
	public WhatsappDBFileRepository(
			@Lazy 
			InitialSetup initialSetup,
			ParallelWebDriverManager parallelWebDriverManager
	) {
		super(initialSetup);
		this.parallelWebDriverManager = parallelWebDriverManager;
		logger.info("WhatsappDBFileRepository initialized with InitialSetup and ParallelWebDriverManager");
	}

	public static void takeBackup(String visitedGroupFileName) {
		//create a backup of contacts file
		File sourceFile = new File(visitedGroupFileName);
		File destinationFile = new File(visitedGroupFileName.replace(".txt", "") + "_backup_"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"))+".txt");
		try {
			FileUtils.copyFile(sourceFile, destinationFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Helper method to get the WebDriver instance when needed
	protected WebDriver getDriver() {
		AutomationContext context = ThreadLocalAutomationContext.getContext();
		if (context == null || context.getTaskType() == null) {
			throw new IllegalStateException("AutomationContext or profile name is not set in ThreadLocal for WhatsappDBFileRepository");
		}
		return parallelWebDriverManager.getDriver(context.getTaskType());
	}

	enum JoinWith {
        COMMA, NEWLINE
    }


	public static void exportToFile(String SUPPORT_EXTRACTED_CONTACTS, Set<String> allNewPhoneContacts, Set<String> successfulGroups, Set<String> errorGroups) {

		Set<String> allNewUnsavedContactsOnly = allNewPhoneContacts.stream().filter(str-> str!=null && !str.isEmpty() && !str.contains("???") && !Character.isLetter(str.charAt(0)) && CONTACT_PREDICATE.test(str)).map(String::trim).distinct().sorted().collect(Collectors.toSet());

		logger.info("allNewUnsavedContactsOnly for new file allNewUnsavedContactsOnly size: "+allNewUnsavedContactsOnly.size());


		//Create/update New File with current Date
		readAndUpdateExistingContactsPresentInFileToMerge(Paths.get(SUPPORT_EXTRACTED_CONTACTS+"\\contacts_backup\\contacts_"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"))+".txt"),allNewUnsavedContactsOnly);

		//Merge the contact and update the existing file
		readAndUpdateExistingContactsPresentInFileToMerge(Paths.get(SUPPORT_EXTRACTED_CONTACTS+"\\contacts.txt"), allNewUnsavedContactsOnly);

		//Write Error if occurs
		writeGroupsWithNewLine(Paths.get(ERROR_GROUPS_TXT),errorGroups);

		//update group_names.txt Removing Error groups
		writeGroupsWithNewLine(Paths.get(ALL_SUPPORT_GROUPS),successfulGroups);
	}

	public static Set<String> getAllGroupsFromCsv(Path path) throws FileNotFoundException, IOException {

		Set<String> allGroupsFromCsv = new HashSet<>();
		try (BufferedReader reader = Files.newBufferedReader(path, CHARSET)) {
			  String line = null;
			  while ((line = reader.readLine()) != null) {
					allGroupsFromCsv.add(line);
			  }
			} catch (IOException e) {
			  e.printStackTrace();
			}
		return allGroupsFromCsv;
	}

    /**
     * Loads group names from a file into a Set
     * @param filePath Path to the file containing group names
     * @return Set of group names
     */
    public static Set<String> loadGroupsFromFile(Path filePath) {
        Set<String> groups = new HashSet<>();
        try {
            if (Files.exists(filePath)) {
                groups = Files.lines(filePath, CHARSET)
                    .filter(line -> line != null && !line.trim().isEmpty())
                    .collect(Collectors.toSet());
                logger.info("Loaded {} groups from file: {}", groups.size(), filePath);
            } else {
                logger.warn("Groups file not found: {}", filePath);
            }
        } catch (IOException e) {
            logger.error("Error loading groups from file: " + filePath, e);
        }
        return groups;
    }

	public static void readAndUpdateExistingContactsPresentInFileToMerge(Path path, Set<String> existingContacts) {
		try (BufferedReader reader = Files.newBufferedReader(path, CHARSET)) {
			String line;
			while ((line = reader.readLine()) != null) {
				// Add the entire line as a single entry after trimming
				if (!line.trim().isEmpty()) {
					existingContacts.add(line.trim());
				}
			}
		} catch (IOException e) {
			logger.info("No existing file to append. Will update freshly. Error: " + e.getMessage());
		}

		// Create a sorted set and trim all entries
		Set<String> existingContactsSorted = existingContacts.stream()
				.filter(Objects::nonNull)
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toCollection(LinkedHashSet::new));

		// Write back the sorted contacts
		writeContactsWithNewLine(path, existingContactsSorted);
	}

	public static void readAndUpdateExistingContactsPresentInFileToMerge_oldBackup(Path path,Set<String> existingContacts) {
		try (BufferedReader reader = Files.newBufferedReader(path, CHARSET)) {
		  String line = null;
		  while ((line = reader.readLine()) != null) {
		    existingContacts.addAll(Arrays.asList(line.split(",")));
		  }
		} catch (IOException e) {
			logger.info("No Existing file to append.So will update Freshly");
		  //e.printStackTrace();
		}

		Set<String> existingContactsSorted= new TreeSet<>(existingContacts);

		existingContactsSorted = existingContactsSorted.parallelStream().map(String::trim).collect(Collectors.toSet());
		writeContactsWithNewLine(path, existingContactsSorted);
	}

	public static void writeGroupsWithNewLine(Path path, Set<String> groups) {
		try (BufferedWriter writer = Files.newBufferedWriter(path, CHARSET)) {
			writer.write(String.join("\n", groups));
			writer.flush();
		} catch (IOException e) {
			logger.info(e.getMessage());
		  //e.printStackTrace();
		}
	}

	public static void writeContactsWithComma(Path path, Set<String> contacts) {
		try (BufferedWriter writer = Files.newBufferedWriter(path, CHARSET)) {
			writer.write(String.join(",", contacts));
			writer.flush();
		} catch (IOException e) {
		  logger.info(e.getMessage());
			//e.printStackTrace();
		}
	}

	public static void writeContactsWithNewLine(Path path, Set<String> contacts) {
		try{
			Files.write(path, String.join("\n", contacts).trim().getBytes());//out of memory
			/*try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
				for (String contact : contacts) {
					writer.write(contact);
					writer.newLine(); // Writes a system-dependent new line character
				}
				logger.info("Contacts written to " + path.toAbsolutePath());*/
			} catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}

	}

	public static boolean clearFileContent(String fileName) {
        return clearContentInFile(fileName);
    }

	public static boolean clearContentInFile(String fileName) {

		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName), CHARSET)) {
			writer.write("");
			writer.flush();
			return true;
		} catch (IOException e) {
			logger.error(e.getMessage());
			return false;
		  //e.printStackTrace();
		}
	}

	public static void exportAndMergeToExistingSingleFile(String fileName, Set<String> setOfData) {
			try {
				Stream<String> stringStream = setOfData.stream().filter(str -> str != null && !str.isEmpty() && !str.contains("???") && containsOnlyPrintableAscii(str));
				if(fileName.contains("contacts")){
					logger.info("contacts file data loading started");
					stringStream = stringStream.filter(CONTACT_PREDICATE);
				}
				Set<String> allGroupsSorted = stringStream.map(String::trim).sorted().collect(Collectors.toCollection(LinkedHashSet::new));
				logger.info("Data set exported to the file : "+fileName+" allGroupsSorted : "+allGroupsSorted+"\n\n\n");
				readAndUpdateExistingContactsPresentInFileToMerge(Paths.get(fileName),allGroupsSorted);
			}catch(Exception ex) {
				ex.printStackTrace();
			}
	}

	private static final Logger logger = LogManager.getLogger(WhatsappDBFileRepository.class);
}
