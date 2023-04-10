package main.java.de.voidtech.gerald.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "nitroliteemote", indexes = @Index(columnList = "name", name = "idx_nitrolite"))
public class NitroliteEmote {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column
	private String name;
	
	@Column
	private String emoteID;
	
	@Column
	private boolean isAnimated;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	NitroliteEmote() {
	}
	
	public NitroliteEmote(String name, String id, boolean isAnimated)
	{
	  this.name = name;
	  this.emoteID = id;
	  this.isAnimated = isAnimated;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String newName) {
		this.name = newName;
	}
	
	public String getID() {
		return emoteID;
	}
	
	public void setID(String newID) {
		this.emoteID = newID;
	}
	
	public boolean isEmoteAnimated() {
		return isAnimated;
	}
	
	public void setIsAnimated(boolean state) {
		this.isAnimated = state;
	}
	
}