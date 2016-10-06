import json
import requests


class SurveyMonkey:
	#the location of the email part in the survey
	EMAIL_ANSWER_INDEX = 3; # position in the answer that contains the email
	IOS_ANSWER_INDEX = 0; # position in the answer that contains the ios question
	EMAIL_QUESTION_ID = '997420484'
	IOS_QUESTION_ID = '997420463'
	
	#hardcoded variables and endpoints
	PEP_RALLY_SURVEY_ID = '83094749'
	API_KEY = "nukeru4m4s3ch73pfhpwsawm"
	ACCESS_TOKEN = "aLDNio953G3ThNP3llk6VW3lVak1w1BrUgyJjUwOZZBPZWe3a7pnxPS0cdEJ5HL.z0XBuU.bS6GOWTLEgqdhiaoMKATcrTWRSyVtSR5pswyeGPcZXwMjzLNX326YXd6hsmvLF-tCPHBbcJ262pQRW1u3ln60xW8EO5BGFP5bD.fwpWi5YiSXIcQvmrQAT8EFlKkQBQJwSLAPWz8hBVQx2jX1Tz42d3cva93-BUMqO7Y="
	HOST = "https://api.surveymonkey.net"
	respondent_uri = "{}/v2/surveys/get_respondent_list".format(HOST)
	response_uri = "{}/v2/surveys/get_responses".format(HOST)
	survey_details_uri = "{}/v2/surveys/get_survey_details".format(HOST)
	survey_list_uri = "{}/v2/surveys/get_survey_list".format(HOST)
        response_list_v3_uri = "{}/v3/surveys/{}/responses";
        response_details_v3_uri = "{}/v3/surveys/{}/responses/{}/details"

	def __init__(self):
		self.client = requests.session()
		self.client.headers = {
		    "Authorization": "bearer {}".format(self.ACCESS_TOKEN),
		        "Content-Type": "application/json",
			}
		self.client.params = {
		    "api_key": self.API_KEY
		    }

	#get a list of surveys and parse the json object to return them
	#returns a list of surveys in an array
	def list_surveys(self):
		data = {};
		response = self.client.post(self.survey_list_uri,data=json.dumps(data))
		jres = response.json()
		return jres['data']['surveys']
		
	#the format of the date is YYYY-MM-DD HH:MM:SS) 
	#this is the v2 api so probably no longer needed
	def get_respondents(self,survey_id=None,page=None,last_modified_date=None):
		if survey_id is None:
			survey_id = self.PEP_RALLY_SURVEY_ID
		post_data = {}
		post_data['survey_id'] = survey_id
		post_data['fields'] = ["date_modified","status"]
		if page is not None:
			post_data["page"] = page
		if last_modified_date is not None:
			post_data["start_modified_date"] = last_modified_date
		response = self.client.post(self.respondent_uri,data=json.dumps(post_data))
		jres = response.json()
		return jres['data']['respondents'] if 'data' in jres else 'omg error'

        #the format of the date is YYYY-MM-DD HH:MM:SS)
	#defaults to 100 responses per page 
	#completed_only says whether or not to only get completed responses
        def get_respondents_v3(self,survey_id=None,page=None,per_page=100,last_modified_date=None,completed_only=True):
                if survey_id is None:
                        survey_id = self.PEP_RALLY_SURVEY_ID
		payload = {}
                if page is not None:
                        payload["page"] = page
                if last_modified_date is not None:
                        payload["start_modified_at"] = last_modified_date
		if completed_only is True:
			payload["status"] =  "completed"
		v3_uri = self.response_list_v3_uri.format(self.HOST,survey_id)
                response = self.client.get(v3_uri,params=payload)
                jres = response.json()
                return jres['data'] if 'data' in jres else 'error getting survey monkey respondents!'

        #the format of the date is YYYY-MM-DD HH:MM:SS)
	#uses the new v3 api which has more info and the correct choice_id for binary questions
        def get_response_details_v3(self,response_id,survey_id=None):
                if survey_id is None:
                        survey_id = self.PEP_RALLY_SURVEY_ID
                v3_uri = self.response_details_v3_uri.format(self.HOST,survey_id,response_id)
                response = self.client.get(v3_uri,params={})
                jres = response.json()
                return jres if 'pages' in jres else 'error getting response details'

	#returns an array of responses?
	#each one corresponds to a given respondent id 
	#the ['respondent_ids'] parameter can only contain up to a max of 100 ids
	def get_responses(self,respondent_ids,survey_id=None):
                if survey_id is None:
                        survey_id = self.PEP_RALLY_SURVEY_ID
		post_data = {}
		post_data['survey_id'] = survey_id
		post_data['respondent_ids'] = respondent_ids
		jres = self.client.post(self.response_uri,data=json.dumps(post_data)).json()
		return jres['data']

	#given the response json from the v3 api, extract the email
        def get_email_from_response_v3(self,response_json):
		email = self.general_extract_field_from_response(response_json,self.EMAIL_QUESTION_ID,self.EMAIL_ANSWER_INDEX,'text')
		return email if email is not None else 'could not find email!'

        #looks like yes is 10456328819
        def get_ios_answer_choice(self,response_json):
		ios_response = self.general_extract_field_from_response(response_json,self.IOS_QUESTION_ID,self.IOS_ANSWER_INDEX,'choice_id')
		return ios_response if ios_response is not None else 'could not find ios response'

	#extract the answer from the json response from the v3 get response details api
	#question_id = id of question, answer_index = where that answer occurs in list
	#field type = what type the answer is for example 'text' or 'choice_id'
        def general_extract_field_from_response(self,response_json,question_id,answer_index,field_type):
		result = None
		for page in response_json:
			for question in page['questions']:
				if question['id'] == question_id:
					result = question['answers'][answer_index][field_type]
		return result

	#given the single response json, extract the email from it
	#return None if email could not be found
	def get_email_from_response(self,response_json):
		email = None;
		for question in response_json['questions']:
			if (question['question_id'] == self.EMAIL_QUESTION_ID):
				email = question['answers'][self.EMAIL_QUESTION_INDEX]['text']
		return email if email is not None else 'could not find email!'
