package desmoj.core.report;

import java.util.Vector;

/**
 * The central object for distributing the messages generated by a simulation
 * run. The MessageDistributor can receive messages and reporters and forwards
 * them to the <code>MessageReceiver</code> objects registered at this
 * MessageDistributor. When registering, the <code>MessageReceiver</code> has
 * to pass a type to identify which type of messages they want to have
 * forwarded. Note that multiple <code>MessageReceiver</code> s can be
 * registered to get the same type of messages as well as a
 * <code>MessageReceiver</code> can be registered with different types of
 * messages, if it is capable of handling such. This enables a modeller i.e. to
 * get the error messages displayed on screen additional to the file being
 * stored on harddisk by default. This is also handy if a simulation should be
 * run as an Applet thus having no or restricted disk access and using multiple
 * scrollable windows instead.
 * 
 * @version DESMO-J, Ver. 2.3.4 copyright (c) 2012
 * @author Tim Lechler
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
public class MessageDistributor implements MessageReceiver {

	/**
	 * The special class for reporters to send all reporters to the experiment's
	 * standard report ouput.
	 */
	private static Class<?> reporters;

	/**
	 * The first item of the list of registered message types.
	 */
	private MLink _head;

	/**
	 * The inner class messagelink keeps track of the types of messages and
	 * their related messagereceiver objects. Designed as an inner class to
	 * messagedistributor, not visible outside.
	 * 
	 * @author Tim Lechler
	 */
	private static class MLink {

		/**
		 * The link to the next link for the next messagetype
		 */
		MLink next;

		/**
		 * The type of message that a messagereceiver object is registered with.
		 */
		Class<?> msgType;

		/**
		 * The Vector filled with messagereceivers registered to receive
		 * messages of the attached type.
		 */
		Vector<MessageReceiver> clients;

		/**
		 * Flag to state whether the current type of message is being
		 * distributed or currently not distributed. Can be set via
		 * <code>SwitchOn()</code> or <code>SwitchOff()</code> methods.
		 */
		boolean isOn;

		/**
		 * Counts the number of future messages to be skipped. This is necessary
		 * to blend out any model activities that are related to internal
		 * operations and thus would confuse the modeller.
		 */
		int skipCount;

		/**
		 * Constructs a link with the given parameters. This is just a
		 * convenient shorthand for setting up the parameters each at a time.
		 */
		MLink(MLink nextLink, Class<?> messageType, boolean showing) {
			isOn = showing; // switches output to receivers on (true) or off
			// (false)
			next = nextLink; // ref to next messageType
			skipCount = 0; // no messages to be skipped now
			msgType = messageType; // the class of the MessageType
			clients = new Vector<MessageReceiver>(3); // max. number of standard clients are
			// 2,
			// so we have one spare for each type

		}

	}

	/**
	 * Constructs a new messagedistributor.
	 */
	public MessageDistributor() {

		super();

		try {
			reporters = Class.forName("desmoj.core.report.Reporter");
		} catch (ClassNotFoundException cnfEx) {
			throw (new desmoj.core.exception.DESMOJException(
					new ErrorMessage(
							null,
							"Can't find class \"desmoj.core.report.Reporter\"!",
							"MessageDistributor-Contructor.",
							"The classfile is probably missing or does not reside in the"
									+ "folder /desmoj/report.",
							"Make sure to have the DESMOJ framework installed correctly",
							null)));
		}

	}

	/**
	 * De-registers the given messagereceiver from all types of Messages it was
	 * registered at. The given messagereceiver is taken from all lists of
	 * messagereceiver.
	 * 
	 * @param out
	 *            MessageReceiver : The messagereceiver to be removed from all
	 *            messages' lists of receivers
	 */
	public void deRegister(MessageReceiver out) {

		// check parameter
		if (out == null)
			return; // invalid param, so just return

		if (_head == null)
			return; // nobody registered yet

		// now scan through all queues and issue removal
		for (MLink tmp = _head; tmp != null; tmp = tmp.next) {

			tmp.clients.removeElement(out);
			if (tmp.clients.isEmpty()) { // if last is taken, trash
				// messagelink

				if (tmp == _head) {
					_head = _head.next; // special care for first element in
					// list
					return; // there can't be any more left to check
				} else {
					tmp = tmp.next; // remove MessageLink
					if (tmp == null)
						return; // there can't be any more left to check
				}

			}

		}

	}

	/**
	 * De-registers a messagereceiver object to stop receiving messages of the
	 * given type. The given messagereceiver object is taken from the list of
	 * messagereceiver receiving messages of the passed messagetype. If invalid
	 * parameters are given (i.e. <code>null</code> references) this method
	 * simply returns
	 * 
	 * @param out
	 *            MessageReceiver : The messagereceiver to be de-registered
	 * @param messageType
	 *            java.lang.Class : The type of messages the messagereceiver
	 *            should be deregistered from
	 */
	public void deRegister(MessageReceiver out, Class<?> messageType) {

		// check parameters
		if (out == null)
			return; // invalid params

		if (messageType == null)
			return; // invalid params

		// get link for messageType
		MLink tmp = linkOf(messageType);

		if (tmp == null)
			return; // not registered, so why bother and return

		// from here on everything must be checked so...
		tmp.clients.removeElement(out); // ...get rid of the client

		if (tmp.clients.isEmpty()) { // if last is taken, trash messagelink

			if (tmp == _head) {
				_head = _head.next; // special care for first element in list
			} else {
				tmp = tmp.next; // remove MessageLink
			}

		}

	}

	/**
	 * De-registers a messagereceiver object to stop receiving messages of the
	 * given type. The given messagereceiver object is taken from the list of
	 * messagereceivers receiving messages of the passed messagetype. If invalid
	 * parameters are given (i.e. <code>null</code> references) this method
	 * simply returns
	 * 
	 * @param out
	 *            MessageReceiver : The messagereceiver to be de-registered
	 * @param className
	 *            String : The type of messages the messagereceiver
	 *            should be deregistered from
	 */
	public void deRegister(MessageReceiver out, String className) {

		// check parameters
		if (out == null)
			return; // invalid params
		if (className == null)
			return; // invalid params

		// get the type
		Class<?> messageType = null;

		try {
			messageType = Class.forName(className);
		} catch (ClassNotFoundException cnfx) {
			return; // send message that class is not in scope
		}

		// get link for messageType
		MLink tmp = linkOf(messageType);
		if (tmp == null)
			return; // not registered, so why bother and retur

		// from here on everything must be checked so...
		tmp.clients.removeElement(out); // ...get rid of the client

		if (tmp.clients.isEmpty()) { // if last is taken, trash messagelink

			if (tmp == _head) {
				_head = _head.next; // special care for first element in list
			} else {
				tmp = tmp.next; // remove MessageLink
			}

		}

	}

	/**
	 * Checks if the current messagetype is switched on to be distributed. If
	 * not, no messages of the given type are distributed or the given type of
	 * message is not registered here.
	 * 
	 * @return boolean : Is <code>true</code> if the type of message is
	 *         distributed <code>false</code> if not or messagetype is not
	 *         registered here
	 */
	public boolean isOn(Class<?> messageType) {

		if (messageType == null)
			return false;

		MLink tmp = linkOf(messageType);

		if (tmp == null)
			return false; // type not registered here

		if (tmp.isOn) {
			return true;
		} else
			return false;

	}

	/**
	 * Checks if the given messagetype is registered at this messagedistributor.
	 * 
	 * @return boolean : Is <code>true</code> if the messagetype is
	 *         registered, <code>false</code> if not
	 */
	public boolean isRegistered(Class<?> messageType) {

		if (messageType == null)
			return false;

		MLink tmp = linkOf(messageType);

		if (tmp == null)
			return false; // type not registered here
		else
			return true;

	}

	/**
	 * Returns the messagelink for the given class or <code>null</code> if the
	 * class is not already registered.
	 * 
	 * @return MessageLink : The messagelink for the given class or
	 *         <code>null</code> if the given class is not registered yet
	 * @param messageType
	 *            java.lang.Class : The class that the link is needed for
	 */
	private MLink linkOf(Class<?> messageType) {

		if (_head == null)
			return null;
		else {

			for (MLink tmp = _head; tmp != null; tmp = tmp.next) {
				if (tmp.msgType == messageType)
					return tmp;
			}

		}

		return null;

	}

	/**
	 * Receives a message and forwards it to all messagereceiver objects
	 * registered with the type of message sent. Messages are sent, if the type
	 * of message is switched on and if the skipCounter is zero, thus not
	 * skipping any messages of that type.
	 * 
	 * @param m
	 *            Message : The message to be forwarded to all registered
	 *            MessageReceivers
	 */
	public void receive(Message m) {

		if (m == null)
			return; // again nulls

		MLink tmp = linkOf(m.getClass()); // get link in list of msgTypes

		if (tmp == null)
			return; // is null if type not registered here, so return???
		
		// checks if the message has to be skipped
		if (tmp.skipCount > 0) {
			tmp.skipCount--;
			return;
		}

		// check if messages of this type should be distributed to their
		// message receivers
		if (!tmp.isOn)
			return;

		// loop + send to all receivers
		for (int i = 0; i < tmp.clients.size(); i++) {
			tmp.clients.elementAt(i).receive(m);
		}

	}

	/**
	 * Receives a reporter and forwards it to all messagereceiver objects
	 * registered with the type of reporter sent.
	 * 
	 * @param r
	 *            Reporter : The reporter to be forwarded to all registered
	 *            messagereceivers.
	 */
	public void receive(desmoj.core.report.Reporter r) {

		if (r == null)
			return; // again nulls

		MLink tmp = linkOf(reporters); // get link in list of msgTypes

		if (tmp == null)
			return; // is null if type not registered here, so return???

		for (int i = 0; i < tmp.clients.size(); i++) { // loop and
			((MessageReceiver) tmp.clients.elementAt(i)).receive(r);
		}

	}

	/**
	 * Registers a messagereceiver object to receive messages of the given type.
	 * 
	 * @param out
	 *            MessageReceiver : The messagereceiver to be registered
	 * @param messageType
	 *            java.lang.Class : The type of messages the messagereceiver is
	 *            registered with
	 */
	public void register(MessageReceiver out, Class<?> messageType) {

		// check parameters
		if (out == null)
			return; // invalid param
		if (messageType == null)
			return; // invalid param

		// now look up for link to registered messageType
		MLink tmp = linkOf(messageType);

		// check link and insert or create new type link
		if (tmp != null) { // type is already known!

			if (tmp.clients.contains(out)) { // check if already reg'd
				return; // already inside
			} else { // client is new to this messagetype, so...
				tmp.clients.addElement(out); // ...add the client
				return; // we're done
			}

		} else { // messageType not registered here, so do it now
			// create the new link. New Links are added at first position
			_head = new MLink(_head, messageType, true);
			_head.clients.addElement(out); // add the output
		}

	}

	/**
	 * Registers a messagereceiver object to receive messages of the given type.
	 * 
	 * @param out
	 *            MessageReceiver : The messagereceiver to be registered
	 * @param className
	 *            java.lang.String : The name of the type of messages the
	 *            messagereceiver is registered with
	 */
	public void register(MessageReceiver out, String className) {

		// check parameters
		if (out == null)
			return; // invalid param
		if ((className == null) || (className.length() == 0))
			return; // invalid param

		// identify the corresponding link
		Class<?> messageType = null;

		try {
			messageType = Class.forName(className);
		} catch (ClassNotFoundException cnfx) {
			return; // send message that class is not in scope???
		}

		// now look up for link to registered messageType
		MLink tmp = linkOf(messageType);

		// check link and insert or create new type link
		if (tmp != null) { // type is already known!

			if (tmp.clients.contains(out)) { // check if already reg'd
				return; // already inside
			} else { // client is new to this messagetype, so...
				tmp.clients.addElement(out); // ...add the client
				return; // we're done
			}

		} else { // messageType not registered here, so do it now
			// create the new link. New Links are added at first position
			_head = new MLink(_head, messageType, true);
			_head.clients.addElement(out); // add the output
		}

	}

	/**
	 * Skips the transmission of the next tracenote or increases the skipCounter
	 * by one. This is necessary to blend out any activities managed by the
	 * framework that would otherwise confuse the modeller.
	 */
	public void skip(Class<?> messageType) {

		if (messageType == null)
			return; // no good parameter

		MLink tmp = linkOf(messageType); // buffer the link to the msgType

		if (tmp == null)
			return; // type not registered, return

		tmp.skipCount++; // well, just increase by one :-)

	}

	/**
	 * Skips the transmission of a number of future messages by increasing the
	 * skipCount by the given number. This is necessary to blend out any
	 * activities managed by the framework that would otherwise confuse the
	 * modeller.
	 * 
	 * @param skipNumber
	 *            int : The number of messages to skip
	 */
	public void skip(Class<?> messageType, int skipNumber) {

		if (skipNumber < 1)
			return; // check parameters for correctness

		if (messageType == null)
			return;

		MLink tmp = linkOf(messageType);

		if (tmp == null)
			return;

		tmp.skipCount += skipNumber; // increase by given number

	}

	/**
	 * Disables messages of the given type to be sent to the registered
	 * receivers.
	 * 
	 * @param messageType
	 *            Class : The type of messages to be switched off
	 */
	public void switchOff(Class<?> messageType) {

		if (messageType == null)
			return; // no good parameter

		MLink tmp = linkOf(messageType); // buffer the link to the msgType

		if (tmp == null)
			return; // type not registered, return

		tmp.isOn = false; // well, just stop sending

	}

	/**
	 * Enables messages of the given type to be sent to the registered
	 * receivers.
	 * 
	 * @param messageType
	 *            Class : The type of messages to be switched on
	 */
	public void switchOn(Class<?> messageType) {

		if (messageType == null)
			return; // no good parameter

		MLink tmp = linkOf(messageType); // buffer the link to the msgType

		if (tmp == null)
			return; // type not registered, return

		tmp.isOn = true; // well, go on and distribute

	}
}