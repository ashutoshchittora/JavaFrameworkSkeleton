package com.student.env.utilities;

public interface UtilityConstants {
	
	int SERVICE_BUS_RETRY  = 2;
	int  SERVICE_BUS_WAITING_TIME = 0;
	int SERVICE_BUS_RETRY_WAITING_TIME = 10000;
	
	boolean MONGO_db_DEFAULT = false;
	int MONGO_db_RETRY  = 2;
	int MONGO_db_WAITING_TIME = 30000;
	int MONGO_db_RETRY_WAITING_TIME = 10000;
	int MONGO_db_default_LIMIT = 0;
	
	int SWAGGER_RETRY = 1;
	int SWAGGER_WAITING_TIME=3000;
	int SWAGGER_RETRY_WAITING_TIME=10000;
	
	int NUMBER_OF_QUERIES = 3;
	
	String EMOTY = " ";
	int FAILURE = 500;
	
	String TEST_DATA_PATH = " //ASDF/GSDFG/SFDSFD/GGSDF/";
	

}
