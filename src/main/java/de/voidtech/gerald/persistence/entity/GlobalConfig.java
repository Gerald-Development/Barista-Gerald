package main.java.de.voidtech.gerald.persistence.entity;


import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;

import javax.persistence.*;


@Entity
@Table(name = "global_config")
public class GlobalConfig  {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column
	private String status;
	
	@Enumerated(EnumType.ORDINAL)
	private ActivityType activity;
	
	public GlobalConfig() {
		this.status = "the coffee machine";
		this.activity = ActivityType.LISTENING;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Activity.ActivityType getActivity() {
		return activity;
	}

	public void setActivity(Activity.ActivityType activity) {
		this.activity = activity;
	}
}
