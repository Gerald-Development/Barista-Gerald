package main.java.de.voidtech.gerald.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.json.JSONObject;

import main.java.de.voidtech.gerald.tasks.TaskType;

@Entity
@Table(name = "delayedtask", indexes = @Index(columnList = "time", name = "idx_delayed_task"))
public class DelayedTask {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column
	@Type(type = "org.hibernate.type.TextType")
	private String args; 
	
	@Column
	private String type;
	
	@Column
	private long time;
	
	@Column
	private String guildID;
	
	@Column
	private String userID;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	DelayedTask() {
	}
	
	public DelayedTask(TaskType type, JSONObject args, String guildID, String userID, long time) {
		this.args = args.toString();
		this.type = type.getType();
		this.time = time;
		this.guildID = guildID;
		this.userID = userID;
	}
	
	public String getTaskType() {
		return this.type;
	}
	
	public long getExecutionTime() {
		return this.time;
	}
	
	public JSONObject getArgs() {
		return new JSONObject(this.args);
	}
	
	public long getTaskID() {
		return this.id;
	}
	
	public String getGuildID() {
		return this.guildID;
	}
	
	public String getUserID() {
		return this.userID;
	}
}