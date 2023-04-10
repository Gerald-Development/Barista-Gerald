package main.java.de.voidtech.gerald.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "riggedlength")
public class RiggedLength {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column
	private String memberID; 
	
	@Column
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
