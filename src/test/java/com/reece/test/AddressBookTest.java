package com.reece.test;

import com.reece.entities.AddressBook;
import com.reece.entities.Contact;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.reece.repositories.AddressBookRepository;
import com.reece.repositories.ContactRepository;
import com.reece.service.ReeceService;
import java.util.Iterator;
import java.util.Set;
import javax.validation.ValidationException;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * JUnit Test Class to Test the Reece Service
 *
 * @author juancarlosbarraganquintero
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring.xml"})
@EnableTransactionManagement
public class AddressBookTest {

    @Autowired
    private ApplicationContext applicationContext;

    private final Logger log = Logger.getLogger(AddressBookTest.class.getName());

    @Autowired
    AddressBookRepository addressBookRepository;

    @Autowired
    ContactRepository contactRepository;

    @Autowired
    ReeceService reeceService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void createAddressBookTest() {
        AddressBook addressBook = reeceService.createAddressBook("Address Book 1");

        AddressBook addressBookFound = addressBookRepository.findOne(addressBook.getId());

        Assert.assertNotNull("Address Book 1 was not saved", addressBookFound);
        Assert.assertEquals("Address Book 1", addressBookFound.getName());
    }

    @Test
    public void createAddressBookNameNullTest() {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("The Address Book Name cannot be null");

        reeceService.createAddressBook(null);
    }

    @Test
    public void createAddressBookNameBlankTest() {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("The Address Book Name must have at least 5 and maximum 30 characters");

        reeceService.createAddressBook("");
    }

    @Test
    public void createAddressBookNameTooShortTest() {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("The Address Book Name must have at least 5 and maximum 30 characters");

        reeceService.createAddressBook("ab");
    }

    @Test
    public void createAddressBookNameTooLongTest() {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("The Address Book Name must have at least 5 and maximum 30 characters");

        reeceService.createAddressBook("1xxxxxxxxxx2xxxxxxxxxx3xxxxxxxxxxY");
    }

    @Test
    @Transactional
    public void addContactToAddressBookTest() {
        reeceService.createAddressBook("Address Book 2");

        reeceService.addContactToAddressBook("Address Book 2", "Carlos", "0425618412");

        // Retrieve the AddressBook Again
        AddressBook addressBook = addressBookRepository.findByName("Address Book 2");

        // Check it is persisted
        Set<Contact> contacts = addressBook.getContacts();

        Assert.assertNotNull("Contacts list is null", contacts);
        Assert.assertEquals("Contact was not persisted", 1, contacts.size());

        Iterator<Contact> contactIterator = contacts.iterator();

        Contact contact = contactIterator.next();

        // Check the contact is not null
        Assert.assertNotNull("Contact is null", contact);

        // Check the contact has the correct values
        Assert.assertNotNull("Contact autogenerated ID is null", contact.getId());
        Assert.assertEquals("Contact name does not match", "Carlos", contact.getName());
        Assert.assertEquals("Contact phone does not match", "0425618412", contact.getPhoneNumber());
    }

    @Test
    @Transactional
    public void addContactNullNameTest() {
        expectedException.expect(Exception.class);
        expectedException.expectMessage("The Contact Name cannot be null");

        reeceService.createAddressBook("Address Book 2");

        reeceService.addContactToAddressBook("Address Book 2", null, "0425618412");

    }
    
    @Test
    @Transactional
    public void addContactNullNameTooShortTest() {
        expectedException.expect(Exception.class);
        expectedException.expectMessage("The Contact Name must have at least 3 and maximum 30 characters");

        reeceService.createAddressBook("Address Book 2");

        reeceService.addContactToAddressBook("Address Book 2", "a", "0425618412");

    }
    
    @Test
    @Transactional
    public void addContactNullPhoneTest() {
        expectedException.expect(Exception.class);
        expectedException.expectMessage("The Contact Phone cannot be null");

        reeceService.createAddressBook("Address Book 2");

        reeceService.addContactToAddressBook("Address Book 2", "Jarrod", null);

    }
    
    @Test
    @Transactional
    public void addContactInvalidPhoneTest() {
        expectedException.expect(Exception.class);
        expectedException.expectMessage("The Contact phone can only contain numbers or hyphens (-)");

        reeceService.createAddressBook("Address Book 2");

        reeceService.addContactToAddressBook("Address Book 2", "Jarrod", "444abc");

    }
    
    @Test
    @Transactional
    public void addContactNullNameTooLongTest() {
        expectedException.expect(Exception.class);
        expectedException.expectMessage("The Contact Name must have at least 3 and maximum 30 characters");

        reeceService.createAddressBook("Address Book 2");

        reeceService.addContactToAddressBook("Address Book 2", "1xxxxxxxxxx2xxxxxxxxxx3xxxxxxxxxxY", "0425618412");

    }

    @Test
    @Transactional
    public void removeContactFromAddressBookTest() throws Exception {
        reeceService.createAddressBook("Address Book 2");

        reeceService.addContactToAddressBook("Address Book 2", "Carlos", "0425618412");
        reeceService.addContactToAddressBook("Address Book 2", "Bill", "0425618413");
        reeceService.addContactToAddressBook("Address Book 2", "Jarrod", "0425618414");

        reeceService.removeContactFromAddressBook("Jarrod", "Address Book 2");

        // Retrieve the AddressBook Again
        AddressBook addressBook = addressBookRepository.findByName("Address Book 2");

        Assert.assertEquals("Contact ", 2, addressBook.getContacts().size());
    }

    @Test
    @Transactional
    public void removeContactNotExistsFromAddressBookTest() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage("Contact with name Blue does not exist in the Address Book Address Book 2");

        reeceService.createAddressBook("Address Book 2");

        reeceService.addContactToAddressBook("Address Book 2", "Carlos", "0425618412");
        reeceService.addContactToAddressBook("Address Book 2", "Bill", "0425618413");
        reeceService.addContactToAddressBook("Address Book 2", "Jarrod", "0425618414");

        reeceService.removeContactFromAddressBook("Blue", "Address Book 2");

        // Retrieve the AddressBook Again
        AddressBook addressBook = addressBookRepository.findByName("Address Book 2");

        Assert.assertEquals("Contact ", 2, addressBook.getContacts().size());
    }

    @Test
    @Transactional
    public void removeContactBookNotExistsTest() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage("Contact with name Jarrod does not exist in the Address Book Address Book 3");

        reeceService.createAddressBook("Address Book 2");

        reeceService.addContactToAddressBook("Address Book 2", "Carlos", "0425618412");
        reeceService.addContactToAddressBook("Address Book 2", "Bill", "0425618413");
        reeceService.addContactToAddressBook("Address Book 2", "Jarrod", "0425618414");

        reeceService.removeContactFromAddressBook("Jarrod", "Address Book 3");

        // Retrieve the AddressBook Again
        AddressBook addressBook = addressBookRepository.findByName("Address Book 2");

        Assert.assertEquals("Contact ", 2, addressBook.getContacts().size());
    }

    public void listAllContactsTest() {
        reeceService.createAddressBook("Address Book 1");
        reeceService.createAddressBook("Address Book 2");

        reeceService.addContactToAddressBook("Address Book 2", "Carlos", "0425618412");
        reeceService.addContactToAddressBook("Address Book 2", "Bill", "0425618413");
        reeceService.addContactToAddressBook("Address Book 2", "Jarrod", "0425618414");
        reeceService.addContactToAddressBook("Address Book 1", "Albert", "0425618415");

        // Find the contacts now
        Contact carlos = contactRepository.findContactByName("Carlos", "Address Book 2");
        Contact bill = contactRepository.findContactByName("Carlos", "Address Book 2");
        Contact jarrod = contactRepository.findContactByName("Carlos", "Address Book 2");
        Contact albert = contactRepository.findContactByName("Carlos", "Address Book 1");

        // Get all contacts from the service
        Set<Contact> contacts = reeceService.getAllContactsAddressBook("Address Book 2");

        Assert.assertTrue(contacts.contains(carlos));
        Assert.assertTrue(contacts.contains(bill));
        Assert.assertTrue(contacts.contains(jarrod));
        Assert.assertFalse(contacts.contains(albert));
    }
}
