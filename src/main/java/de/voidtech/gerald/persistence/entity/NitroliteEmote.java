package main.java.de.voidtech.gerald.persistence.entity;

import javax.persistence.*;

@Entity
@Table(name = "nitroliteemote", indexes = @Index(columnList = "name", name = "idx_nitrolite"))
public class NitroliteEmote {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name="name")
	private String name;
	
	@Column(name="emoteid")
	private String emoteID;
	
	@Column(name="isanimated")
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