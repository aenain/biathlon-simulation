package desmoj.extensions.visualization2d.animation.processStation;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Vector;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.ModelComponent;
import desmoj.core.simulator.TimeInstant;
import desmoj.extensions.visualization2d.animation.CmdGeneration;
import desmoj.extensions.visualization2d.animation.FormExt;
import desmoj.extensions.visualization2d.animation.Position;
import desmoj.extensions.visualization2d.animation.core.simulator.EntityBasicAnimation;
import desmoj.extensions.visualization2d.animation.core.simulator.ModelAnimation;
import desmoj.extensions.visualization2d.animation.core.simulator.ListInterface;
import desmoj.extensions.visualization2d.animation.internalTools.EntryAnimation;
import desmoj.extensions.visualization2d.animation.internalTools.EntryAnimationVector;
import desmoj.extensions.visualization2d.engine.command.Command;
import desmoj.extensions.visualization2d.engine.command.CommandException;
import desmoj.extensions.visualization2d.engine.command.Parameter;
import desmoj.extensions.visualization2d.engine.model.List;

/**
 * Animation of ProcessStation, 
 * there are some SimProcesses as Resource 
 * and some as processed Entities 
 * 
 * @version DESMO-J, Ver. 2.3.4 copyright (c) 2012
 * @author christian.mueller@th-wildau.de
 *         For information about subproject: desmoj.extensions.visualization2d
 *         please have a look at: 
 *         http://www.th-wildau.de/cmueller/Desmo-J/Visualization2d/ 
 * 
 *         Licensed under the Apache License, Version 2.0 (the "License"); you
 *         may not use this file except in compliance with the License. You may
 *         obtain a copy of the License at
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *         implied. See the License for the specific language governing
 *         permissions and limitations under the License.
 *
 * @param <Proc>	SimProcesses as processed Entity
 * @param <Res>		SimProcesses as Resource Entity
 */
public class ProcessStationAnimation
<Proc extends EntityBasicAnimation, Res extends EntityBasicAnimation> extends ModelComponent {

	private boolean 		showInAnimation;
	private CmdGeneration	cmdGen 	= null;
	private Model			model	= null;
	private String			name	= null;
	private EntryAnimationVector<Proc, Res> 	entries	= null;
	private String 			id;
	
	/**
	 * Build a ProcessStation. This Station contains some process entries. 
	 * Each Entry contains some animated process and resource entities.
	 * With insert a new process entry is created and with remove 
	 * a process entry will be deleted.
	 * A process entry looks like a working station.
	 * @param owner				used model
	 * @param name				name of process-station
	 * @param pos				middle point of animation object
	 * @param form				form of animation object
	 * @param showInAnimation	switch animation on or off
	 */
	 public ProcessStationAnimation(ModelAnimation owner, String name, 
				Position pos, FormExt form, boolean showInAnimation){
		 this(owner, name, pos, form, (ListInterface)null, showInAnimation);
	 }

	/**
	 * Build a ProcessStation. This Station contains some process entries. 
	 * Each Entry contains some animated process and resource entities.
	 * With insert a new process entry is created and with remove 
	 * a process entry will be deleted.
	 * A process entry looks like a working station.
	 * @param owner				used model
	 * @param name				name of process-station
	 * @param pos				middle point of animation object
	 * @param form				form of animation object
	 * @param listId			unused. For printing a line between waitinglist and this station
	 *                          use the actual version. 
	 * @param showInAnimation	switch animation on or off
	 */
	@Deprecated
	 public ProcessStationAnimation(ModelAnimation owner, String name, 
				Position pos, FormExt form, String listId, 
				boolean showInAnimation){
		 this(owner, name, pos, form, (ListInterface)null, showInAnimation);
	 }
	 
	 /**
	 * Build a ProcessStation. This Station contains some process entries. 
	 * Each Entry contains some animated process and resource entities.
	 * With insert a new process entry is created and with remove 
	 * a process entry will be deleted.
	 * A process entry looks like a working station.
	 * @param owner				used model
	 * @param name				name of process-station
	 * @param pos				middle point of animation object
	 * @param form				form of animation object
	 * @param list				queue from where process-entities are coming, maybe null
	 * @param showInAnimation	switch animation on or off
	 */
	 public ProcessStationAnimation(ModelAnimation owner, String name, 
			Position pos, FormExt form, ListInterface list, 
			boolean showInAnimation){
		super(owner, name); 
		this.model				= owner;
		this.name				= name;
		this.cmdGen				= owner.getCmdGen();
		this.entries 			= new EntryAnimationVector<Proc, Res>();
		TimeInstant	simTime 		= this.model.presentTime();
		boolean	init			= this.cmdGen.isInitPhase();
		Command c;
		Point p					= pos.getPoint();
		Dimension deltaSize		= form.getDeltaSize();
		this.id					= this.cmdGen.createInternId(name);


		this.showInAnimation	= showInAnimation;
		if(this.showInAnimation){
			try {
				if(init)	c = Command.getCommandInit("createProcessNew", this.cmdGen.getAnimationTime(simTime));
				else 		c = Command.getCommandTime("createProcessNew", this.cmdGen.getAnimationTime(simTime));
				c.addParameter("ProcessId", id);
				c.addParameter("Name", this.name);
				//c.addParameter("Abstract", "" );
				if(list != null)
					c.addParameter("ListId", List.PREFIX_QUEUE+list.getInternId());
				String[] point = {pos.getView(), Integer.toString(p.x), Integer.toString(p.y)};
				c.addParameter("Point", Parameter.cat(point));
				c.addParameter("DefaultEntityType", form.getDefaultType());
				c.addParameter("AnzVisible", Integer.toString(form.getNrVisible()));
				c.addParameter("Form", form.isHorizontal()?"horizontal":"vertikal");
				c.addParameter("ShowResources", "");
				if(deltaSize != null){
					String[] deltaSize1 = {Integer.toString(deltaSize.width), Integer.toString(deltaSize.height)};
					c.addParameter("DeltaSize", Parameter.cat(deltaSize1));
				}
				c.setRemark(this.getGeneratedBy(ProcessStationAnimation.class.getSimpleName()));
				this.cmdGen.checkAndLog(c);
				cmdGen.write(c);
	
			} catch (CommandException e) {
				e.printStackTrace();
			}
		}
	}
	
	 /**
	  * Create a new process entry with process entities and resource entities
	  * @param procEntities		Vector of process entities		
	  * @param resEntities		Vector of resource entities
	  * @return true, when successful
	  */
	 public boolean insert(Vector<Proc> procEntities, Vector<Res> resEntities){
		TimeInstant simTime = this.model.presentTime();
		boolean	init	= this.cmdGen.isInitPhase();
		Command c;
		this.entries.add(new EntryAnimation<Proc, Res>(procEntities, resEntities, null));
		
		boolean out = true;
		String[] procEntityIds = new String[procEntities.size()];
		for(int i=0; i<procEntities.size(); i++) procEntityIds[i] = procEntities.get(i).getName();
		String[] resEntityIds = new String[resEntities.size()];
		for(int i=0; i<resEntities.size(); i++) resEntityIds[i] = resEntities.get(i).getName();

		if(this.showInAnimation){
			try {
				if(init)	c = Command.getCommandInit("setProcessNew", this.cmdGen.getAnimationTime(simTime));
				else 		c = Command.getCommandTime("setProcessNew", this.cmdGen.getAnimationTime(simTime));
				c.addParameter("ProcessId", this.id);
				c.addParameter("AddProcEntity", Parameter.cat(procEntityIds));
				c.addParameter("AddResEntity", Parameter.cat(resEntityIds));
				c.setRemark(ProcessStationAnimation.class.getSimpleName());
				cmdGen.checkAndLog(c);
				cmdGen.write(c);
			} catch (CommandException e1) {
				e1.printStackTrace();
				out = false;
			}
		}
		return out;
	}

	
	 /**
	  * Remove the process entry which contains the entity e.
	  * The entity e can be a process or a resource entity.
	  * All entities of the removed entry are removed also.
	  * @param e
	  * @return the removed Entry, null when not successful
	  */
	 public EntryAnimation<Proc, Res> remove(EntityBasicAnimation e){
		TimeInstant simTime = this.model.presentTime();
		boolean	init	= this.cmdGen.isInitPhase();
		Command c;
		EntryAnimation<Proc, Res> out = this.entries.remove(e);
		if(this.showInAnimation){
			try {
				if(init)	c = Command.getCommandInit("setProcessNew", this.cmdGen.getAnimationTime(simTime));
				else 		c = Command.getCommandTime("setProcessNew", this.cmdGen.getAnimationTime(simTime));
				c.addParameter("ProcessId", this.id);
				c.addParameter("RemoveEntity", e.getName());
				c.setRemark(ProcessStationAnimation.class.getSimpleName());
				cmdGen.checkAndLog(c);
				cmdGen.write(c);
			} catch (CommandException e1) {
				e1.printStackTrace();
				out = null;
			}
		}
		return out;
	}
	
	/**
	 * Gives nr of entries in station. 
	 * @return
	 */
	public int length(){
		return this.entries.length();
	}
	
	/**
	 * Gives names of Proc entities in entry i.
	 * @param i
	 * @return
	 */
	public java.util.List<String> getProcNames(int i) {
		return this.entries.getProcNames(i);
	}

	/**
	 * Gives names of Res entities in entry i.
	 * For each abstract entity an empty string is given.
	 * @param i
	 * @return
	 */
	public java.util.List<String> getResourceNames(int i) {
		return this.entries.getResourceNames(i);
	}

	private String getGeneratedBy(String name){
		String out = "generated by "+name+" and called by ";
		if(this.currentSimProcess() != null)
			out += this.currentSimProcess().getName();
		else
			out += this.currentModel().getName();
		return out;
	}

}
