package com.piisw.tod.config;

import com.piisw.tod.model.*;
import com.piisw.tod.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class DataFiller {

    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final AdRepository adRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;

    public DataFiller(
            UserRepository userRepository,
            TagRepository tagRepository,
            ContactInfoRepository contactInfoRepository,
            AdRepository adRepository,
            MessageRepository messageRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.contactInfoRepository = contactInfoRepository;
        this.adRepository = adRepository;
        this.messageRepository = messageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void fillDatabase() {
        System.out.println("[DataFiller] Starting database initialization...");
        log.info("[DataFiller] Starting database initialization...");
        
        try {
            long userCount = userRepository.count();
            log.info("[DataFiller] Current user count: {}", userCount);
            
            if (userCount > 0) {
                log.info("[DataFiller] Database already contains data. Skipping initialization.");
                System.out.println("[DataFiller] Database already contains data. Skipping initialization.");
                return;
            }

            System.out.println("[DataFiller] Creating tags...");
            log.info("[DataFiller] Creating tags...");

            List<Tag> tags = createTags();

            System.out.println("[DataFiller] Creating users...");
            log.info("[DataFiller] Creating users...");

            List<User> users = createUsers();

            System.out.println("[DataFiller] Creating ads...");
            log.info("[DataFiller] Creating ads...");

            createAds(users, tags);

            System.out.println("[DataFiller] Creating messages...");
            log.info("[DataFiller] Creating messages...");

            createMessages(users);

            System.out.println("[DataFiller] Verifying data...");
            log.info("[DataFiller] Verifying data...");

            verifyData();

            System.out.println("[DataFiller] Database successfully populated with test data!");
            log.info("[DataFiller] Database successfully populated with test data!");
        } catch (Exception e) {
            System.out.println("[DataFiller] ERROR: " + e.getMessage());
            log.error("[DataFiller] Error during data initialization", e);
            throw e;
        }
    }

    private List<Tag> createTags() {
        List<Tag> tags = Arrays.asList(
                Tag.builder().name("electronics").description("Electronic items and devices").build(),
                Tag.builder().name("furniture").description("Furniture and home furnishings").build(),
                Tag.builder().name("clothing").description("Clothing and accessories").build(),
                Tag.builder().name("books").description("Books and magazines").build(),
                Tag.builder().name("sports").description("Sports and fitness items").build(),
                Tag.builder().name("automotive").description("Cars and automotive accessories").build(),
                Tag.builder().name("home").description("Home and garden items").build(),
                Tag.builder().name("toys").description("Games and toys").build(),
                Tag.builder().name("beauty").description("Cosmetics and beauty products").build(),
                Tag.builder().name("pets").description("Pets and pet accessories").build()
        );
        tagRepository.saveAll(tags);
        log.info("[DataFiller] Created {} tags", tags.size());
        return tags;
    }

    private List<User> createUsers() {
        User user1 = User.builder()
                .email("john.smith@example.com")
                .password(passwordEncoder.encode("password123"))
                .build();
        user1 = userRepository.save(user1);
        log.info("[DataFiller] Created user: john.smith@example.com");

        ContactInfo contact1_phone = ContactInfo.builder()
                .type("PHONE")
                .value("+44 7911 123456")
                .user(user1)
                .build();
        ContactInfo contact1_email = ContactInfo.builder()
                .type("EMAIL")
                .value("john.smith@gmail.com")
                .user(user1)
                .build();
        ContactInfo contact1_address = ContactInfo.builder()
                .type("ADDRESS")
                .value("42 Oxford Street, London, UK W1D 1AN")
                .user(user1)
                .build();
        user1.setContactInfos(Arrays.asList(contact1_phone, contact1_email, contact1_address));
        contactInfoRepository.saveAll(Arrays.asList(contact1_phone, contact1_email, contact1_address));

        User user2 = User.builder()
                .email("sarah.jones@example.com")
                .password(passwordEncoder.encode("password456"))
                .build();
        user2 = userRepository.save(user2);
        log.info("[DataFiller] Created user: sarah.jones@example.com");

        ContactInfo contact2_phone = ContactInfo.builder()
                .type("PHONE")
                .value("+44 7922 654321")
                .user(user2)
                .build();
        ContactInfo contact2_address = ContactInfo.builder()
                .type("ADDRESS")
                .value("123 Main Street, Manchester, UK M1 1AE")
                .user(user2)
                .build();
        user2.setContactInfos(Arrays.asList(contact2_phone, contact2_address));
        contactInfoRepository.saveAll(Arrays.asList(contact2_phone, contact2_address));

        User user3 = User.builder()
                .email("michael.brown@example.com")
                .password(passwordEncoder.encode("password789"))
                .build();
        user3 = userRepository.save(user3);
        log.info("[DataFiller] Created user: michael.brown@example.com");

        ContactInfo contact3_phone = ContactInfo.builder()
                .type("PHONE")
                .value("+44 7933 987654")
                .user(user3)
                .build();
        user3.setContactInfos(List.of(contact3_phone));
        contactInfoRepository.save(contact3_phone);

        log.info("[DataFiller] Created 3 users");
        return Arrays.asList(user1, user2, user3);
    }

    private void createAds(List<User> users, List<Tag> tags) {
        User user1 = users.get(0);
        User user2 = users.get(1);
        User user3 = users.get(2);

        Ad ad1 = Ad.builder()
                .title("Dell XPS 13 Laptop - Nearly New")
                .description("Selling a Dell XPS 13 from 2023. Excellent condition with no damage. " +
                        "FHD display, Intel Core i7 processor, 16GB RAM, 512GB SSD. Includes original charger and protective case. " +
                        "Price is negotiable.")
                .status(AdStatus.PUBLISHED)
                .author(user1)
                .createdAt(LocalDateTime.now().minusDays(5))
                .updatedAt(LocalDateTime.now().minusDays(5))
                .build();
        ad1.getTags().add(tags.getFirst());
        ad1.getContactInfos().add(user1.getContactInfos().getFirst());
        adRepository.save(ad1);

        Ad ad2 = Ad.builder()
                .title("3+1 Seater Corner Sofa - Latest Model")
                .description("Beautiful and comfortable 3+1 corner sofa in grey. Perfect for living rooms. " +
                        "Dimensions: 2.5m width. Excellent condition. Negotiable price.")
                .status(AdStatus.PUBLISHED)
                .author(user2)
                .createdAt(LocalDateTime.now().minusDays(3))
                .updatedAt(LocalDateTime.now().minusDays(3))
                .build();
        ad2.getTags().addAll(Arrays.asList(tags.get(1), tags.get(6)));
        ad2.getContactInfos().addAll(user2.getContactInfos());
        adRepository.save(ad2);

        Ad ad3 = Ad.builder()
                .title("Trek Mountain Bike - Unused")
                .description("New, unused Trek mountain bike. XL frame size. " +
                        "Only missing tyres from the complete set. Perfect for bike enthusiasts.")
                .status(AdStatus.DRAFT)
                .author(user1)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
        ad3.getTags().add(tags.get(4));
        ad3.getContactInfos().add(user1.getContactInfos().get(1));
        adRepository.save(ad3);

        Ad ad4 = Ad.builder()
                .title("Science Fiction Books Collection")
                .description("Selling a collection of sci-fi books in original English editions. " +
                        "Authors: Asimov, Clarke, Heinlein and others. Condition: very good to excellent.")
                .status(AdStatus.PUBLISHED)
                .author(user3)
                .createdAt(LocalDateTime.now().minusDays(7))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build();
        ad4.getTags().add(tags.get(3));
        ad4.getContactInfos().add(user3.getContactInfos().getFirst());
        adRepository.save(ad4);

        Ad ad5 = Ad.builder()
                .title("Bosch Washing Machine - SOLD")
                .description("Bosch 8kg washing machine - SOLD")
                .status(AdStatus.ARCHIVED)
                .author(user2)
                .createdAt(LocalDateTime.now().minusDays(30))
                .updatedAt(LocalDateTime.now().minusDays(20))
                .build();
        ad5.getTags().add(tags.get(6));
        adRepository.save(ad5);

        Ad ad6 = Ad.builder()
                .title("Estée Lauder Cosmetics Set - New")
                .description("Complete, unopened cosmetics set by premium brand Estée Lauder. " +
                        "Perfect as a gift. Contains cream, serum, mask and more.")
                .status(AdStatus.PUBLISHED)
                .author(user1)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build();
        ad6.getTags().add(tags.get(8));
        ad6.getContactInfos().addAll(user1.getContactInfos());
        adRepository.save(ad6);

        Ad ad7 = Ad.builder()
                .title("Baby Pram 4 in 1")
                .description("Modern Kinderkraft 4 in 1 pram: cradle, pushchair, car seat. " +
                        "Almost new, excellent condition. Price negotiable.")
                .status(AdStatus.PUBLISHED)
                .author(user2)
                .createdAt(LocalDateTime.now().minusDays(4))
                .updatedAt(LocalDateTime.now().minusDays(4))
                .build();
        ad7.getTags().add(tags.get(7));
        ad7.getContactInfos().add(user2.getContactInfos().getFirst());
        adRepository.save(ad7);

        log.info("[DataFiller] Created 7 ads");
    }

    private void createMessages(List<User> users) {
        User user1 = users.get(0);
        User user2 = users.get(1);
        User user3 = users.get(2);

        Message msg1 = Message.builder()
                .content("Hi! Is the laptop still available? Can I arrange a viewing?")
                .sentAt(LocalDateTime.now().minusHours(5))
                .isRead(true)
                .sender(user2)
                .receiver(user1)
                .build();
        messageRepository.save(msg1);

        Message msg1Reply = Message.builder()
                .content("Yes, the laptop is available! I can arrange a meeting this evening or tomorrow morning.")
                .sentAt(LocalDateTime.now().minusHours(4))
                .isRead(true)
                .sender(user1)
                .receiver(user2)
                .parentMessage(msg1)
                .build();
        messageRepository.save(msg1Reply);

        Message msg2 = Message.builder()
                .content("How does the sofa look? What are the exact dimensions?")
                .sentAt(LocalDateTime.now().minusHours(3))
                .isRead(true)
                .sender(user3)
                .receiver(user2)
                .build();
        messageRepository.save(msg2);

        Message msg2Reply = Message.builder()
                .content("The sofa is 250x100cm. I'll send you photos shortly.")
                .sentAt(LocalDateTime.now().minusHours(2))
                .isRead(false)
                .sender(user2)
                .receiver(user3)
                .parentMessage(msg2)
                .build();
        messageRepository.save(msg2Reply);

        Message msg3 = Message.builder()
                .content("I'm interested in the sci-fi books. Are all of them available?")
                .sentAt(LocalDateTime.now().minusHours(1))
                .isRead(true)
                .sender(user1)
                .receiver(user3)
                .build();
        messageRepository.save(msg3);

        log.info("[DataFiller] Created 5 messages");
    }

    private void verifyData() {
        var user1 = userRepository.findByEmail("john.smith@example.com");
        if (user1.isPresent()) {
            log.info("[DataFiller] Verification: john.smith@example.com found in database");
            boolean passwordMatches = passwordEncoder.matches("password123", user1.get().getPassword());
            if (passwordMatches) {
                log.info("[DataFiller] Verification: Password matches for john.smith@example.com");
            } else {
                log.warn("[DataFiller] Verification: Password mismatch for john.smith@example.com");
            }
        } else {
            log.warn("[DataFiller] Verification: john.smith@example.com NOT found in database!");
        }
    }
}
