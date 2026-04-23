package com.wtf.app.app.commons;

import com.wtf.app.app.com.parent.Constants;
import com.wtf.app.app.enums.SocialType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WhatsappContactUtils extends Constants{

	public static final Predicate<String> CONTACT_PREDICATE = str -> !Character.isLetter(str.charAt(0)) && !Character.isLetter(str.charAt(1)) && str.length() < 50;

	public WhatsappContactUtils(String baseURL) {
		super(baseURL, SocialType.WHATSAPP);
	}

	enum JoinWith
	    {
	        COMMA, NEWLINE;
	    }
	private static final Charset charset = StandardCharsets.ISO_8859_1;

	private static final String SUPPORT_EXTRACTED_CONTACTS = "D:\\SupportDetails\\Support_ExtractSupportProxyContacts\\contacts";



	public static void exportToFile(String SUPPORT_EXTRACTED_CONTACTS, Set<String> allNewPhoneContacts, Set<String> successfulGroups, Set<String> errorGroups) {

		Set<String> allNewUnsavedContactsOnly = allNewPhoneContacts.stream().filter(str-> str!=null && !str.isEmpty() && !str.contains("???") && !Character.isLetter(str.charAt(0)) && CONTACT_PREDICATE.test(str)).map(String::trim).distinct().sorted().collect(Collectors.toSet());

		logger.fine("allNewUnsavedContactsOnly for new file allNewUnsavedContactsOnly size: "+allNewUnsavedContactsOnly.size());


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
		try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
			  String line = null;
			  while ((line = reader.readLine()) != null) {
					allGroupsFromCsv.add(line);
			  }
			} catch (IOException e) {
			  e.printStackTrace();
			}
		return allGroupsFromCsv;
	}


	public static void readAndUpdateExistingContactsPresentInFileToMerge(Path path,Set<String> existingContacts) {
		try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
		  String line = null;
		  while ((line = reader.readLine()) != null) {
		    existingContacts.addAll(Arrays.asList(line.split(",")));
		  }
		} catch (IOException e) {
			logger.fine("No Existing file to append.So will update Freshly");
		  //e.printStackTrace();
		}

		Set<String> existingContactsSorted= new TreeSet<>(existingContacts);

		existingContactsSorted = existingContactsSorted.parallelStream().map(String::trim).collect(Collectors.toSet());
		writeContactsWithNewLine(path, existingContactsSorted);
	}

	public static void writeGroupsWithNewLine(Path path, Set<String> groups) {
		try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
			writer.write(String.join("\n", groups));
			writer.flush();
		} catch (IOException e) {
			logger.fine(e.getMessage());
		  //e.printStackTrace();
		}
	}

	public static void writeContactsWithComma(Path path, Set<String> contacts) {
		try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
			writer.write(String.join(",", contacts));
			writer.flush();
		} catch (IOException e) {
		  logger.fine(e.getMessage());
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
				logger.fine("Contacts written to " + path.toAbsolutePath());*/
			} catch (IOException e) {
				logger.severe(e.getMessage());
				e.printStackTrace();
			}

	}

	public static void clearContentInFile(String fileName) {

		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName), charset)) {
			writer.write("");
			writer.flush();
		} catch (IOException e) {
			logger.severe(e.getMessage());
		  //e.printStackTrace();
		}
	}

	public static void exportAndMergeToExistingSingleFile(String fileName, Set<String> setOfData) {
			try {
				Stream<String> stringStream = setOfData.stream().filter(str -> str != null && !str.isEmpty() && !str.contains("???") && containsOnlyPrintableAscii(str));
				if(fileName.contains("contacts")){
					logger.fine("contacts file data loading started");
					stringStream = stringStream.filter(CONTACT_PREDICATE);
				}
				Set<String> allGroupsSorted = stringStream.map(String::trim).sorted().collect(Collectors.toCollection(LinkedHashSet::new));
				logger.fine("Data set exported to the file : "+fileName+" allGroupsSorted : "+allGroupsSorted+"\n\n\n");
				readAndUpdateExistingContactsPresentInFileToMerge(Paths.get(fileName),allGroupsSorted);
			}catch(Exception ex) {
				ex.printStackTrace();
			}
	}

	}
