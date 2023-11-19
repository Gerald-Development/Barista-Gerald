package main.java.de.voidtech.gerald.persistence.entity;

import javax.persistence.*;

@Entity
@Table(name = "riggedlength")
public class RiggedLength {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name="memberid")
	private String memberID; 
	
	@Column(name="length")
	private int length;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	RiggedLength() {
	}
	
	public RiggedLength(String memberID, int length) {
		this.memberID = memberID;
		this.length = length;
	}
	
	public int getLength() {
		return this.length;
	}
	
	public void setLength(int length) {
		this.length = length;
	}
}
