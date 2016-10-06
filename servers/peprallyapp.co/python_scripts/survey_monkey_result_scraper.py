
#script that gets responses from survey monkey and updates dynamo with the latest responses
#can pass in a date and then the script only looks for responses that were completed after that
#given date
#todo: maybe look for responses that were partially completed but still answer questions we care about

import argparse
from SurveyMonkey import *
from PepRallyDynamoDB import *

parser = argparse.ArgumentParser()
parser.add_argument('-d', '--last_modified_date', default='2016-08-01 00:00:00')

args = parser.parse_args()
survey_monkey = SurveyMonkey()
#max_modified_respondent_date = args.last_modified_date
sm_dynamo_db = PepRallyDynamoDB()

respondents_cur_page = 1
while True:
    print 'searching for respondents with date after {} and current page {}'.format(args.last_modified_date,respondents_cur_page)
    respondent_json = survey_monkey.get_respondents_v3(last_modified_date=args.last_modified_date,page=respondents_cur_page)
    if len(respondent_json) == 0:
	print 'no more respondents to get...terminating script!'
        break

    respondent_ids = []
    for respondent in respondent_json:
        respondent_ids.append(respondent["id"])

    #loop through all the response ids and get the details
    for id in respondent_ids:
	response_details_full = survey_monkey.get_response_details_v3(id)
	response_detail_pages = response_details_full['pages']
	email = survey_monkey.get_email_from_response_v3(response_detail_pages)
	ios_response = survey_monkey.get_ios_answer_choice(response_detail_pages)
        print 'Writing entry for respondent_id: {} to the db'.format(id)

	#writing the entire response data doesn't work b/c then there's some empty fields
	#that's why we only use the ['pages'] one
        sm_dynamo_db.write_survey_results(id,args.last_modified_date,email,raw_json=response_detail_pages,ios_answer=ios_response,ip_addr=response_details_full['ip_address'])

    print('Current iteration finished. Increasing page num...')
    respondents_cur_page += 1
