import boto.dynamodb2
from boto.dynamodb2.table import Table
from boto.dynamodb2.exceptions import ConditionalCheckFailedException

class PepRallyDynamoDB:

	#hardcoded variables
	AWS_REGION = "us-east-1"
	SURVEY_RESULTS_TABLE = "SurveyResults"

	def __init__(self):
		#keys found in ~/.aws/credentials file with profile pep-rally
		#see http://boto.cloudhackers.com/en/latest/boto_config_tut.html for format
		self.conn = boto.dynamodb2.connect_to_region(self.AWS_REGION,profile_name="pep-rally")
		self.conn.list_tables()	
		self.survey_table = Table(self.SURVEY_RESULTS_TABLE,connection=self.conn)

	#hashkey is RESPONDENT_ID
	#rangekey is DATE_MODIFIED
	def write_survey_results(self,respondent_id,date_modified,email,raw_json=None,ios_answer=None,ip_addr = None):
		item_data = {}
		item_data['RESPONDENT_ID'] = respondent_id
		item_data['DATE_EXTRACTED'] = date_modified
		item_data['EMAIL_ADDRESS'] = email
		if raw_json is not None:
			item_data['raw_response'] = raw_json
		if ios_answer is not None:
			item_data['ios_answer_choice'] = ios_answer
		if ip_addr is not None:
			item_data['ip_address'] = ip_addr
		try: 
			self.survey_table.put_item(data=item_data)
		except ConditionalCheckFailedException,e:
			print 'Respondent_id: {} already exists in the db!'.format(respondent_id)
