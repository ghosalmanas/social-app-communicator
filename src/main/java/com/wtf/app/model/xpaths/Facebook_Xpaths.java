package com.wtf.app.model.xpaths;

import com.wtf.app.model.enums.SocialType;
import org.openqa.selenium.Keys;

public class Facebook_Xpaths implements XPathInterfaceFB {
    private static final String FACEBOOK_SCANNER_IDENTIFIER = "//title[text()='Facebook']";
    private final String BASE_URL = "https://www.facebook.com/";
    // Set up Facebook login account name and password
    private final String email = "ghosalmanas80@gmail.com";
    private final String password = "srimanta";
    private final String groupsLink = "https://www.facebook.com/groups/";

    private final String emailId = "//input[@type='text' and @placeholder='Email address or phone number']";

    private final String MESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT = //"" + (Keys.SHIFT)
            //+ (Keys.ENTER) + (Keys.SHIFT)
            "I am an individual *Fullstack Proxy and Support (Direct Person) with 16+ Years* of Expertise IT Industry Experience. WhatsApp: *+918274848227* "
                    //+ "and 7 Years Experience "
                    + " _NL_ "
                    + " _NL_ "
                    + "**GUARANTEED, SAFE AND SECURE** I take calls from *USA, Canada, UK and India since last 9+ years* in Fullstack and Backend profiles in below areas :"
                    + " _NL_ "
                    + "*INTERVIEW SUPPORT (PROXY Calls)*/Assignment/Job Support/Coding Test/Training/Task"
                    + " _NL_ "
                    + " _NL_ "
                    + /*(Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT) +*/ "## *WhatsApp: +918274848227*"
                    + " _NL_ "
                    //+ (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT) + "Telegram:- https://t.me/ghosalakash"

                    //+ (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT)
                    + " _NL_ "
                    + " _NL_ "
                    + "*100% GENUINE : Feel free to Ask me any Technical Question, I am always ready to face and answer*"
                    + " _NL_ "
                    + " _NL_ "
                    //+ "Tech Stack : JAVA 8,17,21, Spring Boot, Microservices, Kafka, Angular, React, Javascript, AWS, Docker, Kubernetes, JPA, Redis, DevOps, SQL & NoSQL, UNIX, JUnit, Mockito, Selenium, CICD, Data Structure & Algorithms "
                    //+ (Keys.SHIFT) + (Keys.ENTER)
                    + "*Technology Stacks* :"
                    + " _NL_ "
                    + "  1. *JAVA 8,11,17,21,24 and Complex Codings and LeetCode problem solving*"
                    + " _NL_ "
                    + "  2. Spring, Spring Boot, Spring MVC, Spring AI"
                    + " _NL_ "
                    + "  3. Microservices, Soap, Rest Api"
                    + " _NL_ "
                    + "  5. *Angular, React, Javascript*"
                    + " _NL_ "
                    + "  4. Kafka, Spark"
                    + " _NL_ "
                    + "  6. AWS/Azure/GCP, DevOps, CICD"
                    + " _NL_ "
                    + "  7. Docker, Kubernetes, Redis, JPA, SQL and NoSQL Databases"
                    + " _NL_ "
                    + "  8. *Data Structures and Algorithms*"
                    + " _NL_ "
                    + " _NL_ "
                    + "Note :" +
                    "All Apps are used of *Premium Version and 100% _IN-VISIBLE and UN-TRACEABLE_ from anywhere*"
                    + " _NL_ "
                    + "*Contact me directly if you need any kind of help, WhatsApp: +918274848227*";
    //+ (Keys.SHIFT) + (Keys.ENTER);
    /*
     * +(Keys.SHIFT)+(Keys.ENTER) +
     * "** A Professional Java Fullstack Proxy** use - **Prompting & Transcript with Premium Otter** wth Screen Control for live coding."
     * +(Keys.SHIFT)+(Keys.ENTER) +
     * "**Domain Experiences**: Banking, Capital Market/Securities Trading, Telecom, Healthcare, Aviation"
     */
    /*
     * +(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
     * +(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT) +
     * "**Expert* in *Java 8, Java 11, Java 17, Spring boot, Microservices, Kafka*, *Data Structures and Algorithms*, Hibernate,"
     * + " *JPA, Docker, Kubernetes*, *AWS*, UI: *Angular*, React,**" +
     * " Node JS, *Redis Cache, SQL DBs (Oracle, SqlServer, MySQL, Postgress, Cockroach DB)*, *NoSQL DBs(Couchbase, MongoDB, Cassandra)*, Spring MVC, Spring AOP,"
     * +
     * " *Spring Security*, Auth Services (OAuth2 with OpenId Connect, OAuth with Google Api, LDAP, SSO, *JWT*, OKTA), *SQL, PL-SQL*, ELK,"
     * +
     * " CICD: *Gitlab, Bamboo, Jenkins*, TFS, DevOps, C#, CDC with Debezium and Kafka Connect, Splunk with HEC, Salesforce Data integration,"
     * +
     * " Git, GitHub, BitBucket, *Junit, Mockito, MockMvc, PowerMock, EasyMock*, XLDEPLOY, Appian Low Code, FIX protocol"
     * +
     * " (Amazon web Service EC2, S3, Route 53, Kinesis, ECS, EKS, *Lambda*, RDS, SQS, SNS, SES)"
     * + " and in Various Technologies"
     */

    // + "** Support Process: Two Supports are allocated for a single consultant for
    // Delivery Assurance";
    // " NOTE: Be Aware of Fake People with Fake Experiences. Verify them by asking
    // their Linkedin profile and crosscheck industry experience"

    private final String MESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_WELCOMING_OFFSHORE_FREELANCER_IN_ALL_TECHS = ""
            + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT)
            + "We have requirements for direct Support and Proxy persons for the below technologies " + (Keys.SHIFT)
            + (Keys.ENTER) + (Keys.SHIFT) + "1. Java/NodeJS with Angular/React" + (Keys.SHIFT) + (Keys.ENTER)
            + (Keys.SHIFT) + "2. DevOps with AWS/Azure/GCP" + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT)
            + "4. Salesforce" + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT) + "5. Big Data, Scala and Spark"
            + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT) + "6. Data Science and Data Engineer" + (Keys.SHIFT)
            + (Keys.ENTER) + (Keys.SHIFT) + "7. Power BI & SSRS" + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT)
            + "8. Dot Net with Azure" + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT) + "9. Automation Testing"
            + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT) + "10. AWS/Azure/GCP admins" + (Keys.SHIFT) + (Keys.ENTER)
            + (Keys.SHIFT) + "11. Pega" + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT) + "12. Mulesoft" + (Keys.SHIFT)
            + (Keys.ENTER) + (Keys.SHIFT) + "13. ETL" + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT) + "14. Service Now"
            + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT) + "15. Golang" + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT)
            + "16. C++, vscode, putty" + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT) + "17. SAP" + (Keys.SHIFT)
            + (Keys.ENTER) + (Keys.SHIFT) + "18. Analytics and Machine Learning" + (Keys.SHIFT) + (Keys.ENTER)
            + (Keys.SHIFT) + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT)
            + "Please share the techstack with total years of IT experience and years of experience in�support/proxy"
            + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT) + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT)
            + "Whatsapp:- https://wa.me/+9836509607" + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT) + (Keys.SHIFT)
            + (Keys.ENTER) + (Keys.SHIFT) + "Email:- santoshkumarcloudtech@gmail.com" + (Keys.SHIFT) + (Keys.ENTER);


    private final String MESSAGE_TO_EACH_RESTRICTED_SENSITIVE_TRAINING_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT_ANNONYM =
            "" + (Keys.SHIFT)
                    + (Keys.ENTER) + (Keys.SHIFT)
                    + "I have 15+ Years of Expertise IT Industry Experience and 10 Long Years of Experience in Freelancing with Training, Support, Assessment, Coding Test, Interview Preparation and Technical help in Java Fullstack with Angular, React and Java Backend with Spring Boot, Microservices, Kafka etc."
                    + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT) + "Contact me for details. WhatsApp:- https://wa.me/+918274848227"
                    //+ (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT) + "Telegram:- https://t.me/ghosalakash"

                    + (Keys.SHIFT) + (Keys.ENTER) + (Keys.SHIFT)
                    + "Tech Stack : JAVA 8,11,17,21, Spring Boot, Microservices, Kafka, Angular, React, Javascript, AWS, Docker, Kubernetes, JPA, Redis, DevOps, SQL & NoSQL, UNIX, JUnit, Mockito, Selenium, CICD, Low Code, Data Structure & Algorithms "
                    + (Keys.SHIFT) + (Keys.ENTER)
                    + "** Please contact me directly if you need any kind of help in freelancing**"
                    + (Keys.SHIFT) + (Keys.ENTER);

    public Facebook_Xpaths() {
        super();
    }

    @Override
    public String getMESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT() {
        return MESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT;
    }

    @Override
    public String getMESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_WELCOMING_OFFSHORE_FREELANCER_IN_ALL_TECHS() {
        return MESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_WELCOMING_OFFSHORE_FREELANCER_IN_ALL_TECHS;
    }

    @Override
    public String getMESSAGE_TO_EACH_RESTRICTED_SENSITIVE_TRAINING_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT_ANNONYM() {
        return MESSAGE_TO_EACH_RESTRICTED_SENSITIVE_TRAINING_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT_ANNONYM;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getGroupsLink() {
        return groupsLink;
    }

    @Override
    public String getEmailId() {
        return emailId;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getBASE_URL() {
        return BASE_URL;
    }

    @Override
    public String getSOCIAL_MEDIA_SCANNER_IDENTIFIER_AFTER_URL() {
        return FACEBOOK_SCANNER_IDENTIFIER;
    }

    @Override
    public SocialType getSocialType() {
        return SocialType.FACEBOOK;
    }


}