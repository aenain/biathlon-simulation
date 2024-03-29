package desmoj.extensions.visualization2d.engine.modelGrafic;

/**
 * describes the position of an entity
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
 */
public class EntityPosition {

	private double positionX;
	private double positionY;
	private double angle;
	private boolean direction;
	
	/**
	 * 
	 * @param positionX		
	 * @param positionY
	 * @param angle			rotation angle
	 * @param direction		icon is not reflected
	 */
	public EntityPosition(double positionX, double positionY, double angle, boolean direction){
		this.positionX	= positionX;
		this.positionY	= positionY;
		this.angle		= angle;
		this.direction	= direction;
	}
	
	public EntityPosition(){
		this.positionX	= 0.0;
		this.positionY	= 0.0;
		this.angle		= 0.0;
		this.direction	= true;
	}
	
	public void setX(double x){
		this.positionX	= x;
	}
	
	public double getX(){
		return this.positionX;
	}
	
	public void setY(double y){
		this.positionY	= y;
	}
	
	public double getY(){
		return this.positionY;
	}
	
	public void setAngle(double angle){
		this.angle		= angle;
	}
	
	public double getAngle(){
		return this.angle;
	}

	public void setDirection(boolean direction){
		this.direction		= direction;
	}
	
	public boolean getDirection(){
		return this.direction;
	}
}
