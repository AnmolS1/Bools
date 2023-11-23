import re
from datetime import datetime

def save(is_first, variables, expression, table, history):
	exp_len = len(expression)
	tab_len = (len(variables) + 2) * 2 + 1
	num_dashes = max(exp_len, tab_len) + 2
	
	def choose_not_to_save():
		user_save_choice = None
		while True:
			try:
				if is_first:
					user_save_choice = int(input('do you want to save this table?\n\t1) yes\n\t2) no\n:'))
				else:
					user_save_choice = int(input('1) pls save\n2) noo save\n: '))
				
				if type(user_save_choice) == int and (0 < user_save_choice < 3):
					break
			except Exception as e:
				print('please enter a valid option')
				continue
		print()
		
		return user_save_choice == 2
	
	def get_spacing(obj_len):
		spacing = int((num_dashes - obj_len - 2) / 2)
		odd = (num_dashes - obj_len) % 2 == 1
		return ' ' * (spacing + (1 if odd else 0)), ' ' * spacing
	
	if choose_not_to_save():
		return history
	
	if history != '':
		history += '\n\n'
	
	border_dashes = '━' * num_dashes
	header_left_spaces, header_spaces = get_spacing(exp_len)
	table_left_spaces, table_spaces = get_spacing(tab_len)
	
	table = table.replace('\n', table_spaces + ' ┃\n┃ ' + table_left_spaces)
	
	history += '┏' + border_dashes + '┓\n'
	history += '┃ ' + header_left_spaces + expression + header_spaces + ' ┃\n'
	history += '┣' + border_dashes + '┫\n'
	history += '┃ ' + table_left_spaces + table + table_spaces + ' ┃\n'
	history += '┗' + border_dashes + '┛'
	
	return history

def convert(x):
	return 'T' if x else 'F'

def pretty_truth_table(table, variables):
	num_variables, ret = len(variables), ' '
	
	ret += ''.join([f'{var} ' for var in variables]) + '|   \n-' + ('--' * (num_variables + 2)) + '\n'
	
	for r in range(len(table)):
		ret += ' '
		for c in range(num_variables):
			ret += f'{convert(table[r][c])} '
		ret += f'| {convert(table[r][-1])} \n'
	
	return ret[:-1]

def get_all_combos(num_variables):
	num_rows = 2 ** num_variables
	
	table = [[False for c in range(num_variables + 1)] for r in range(num_rows)]
	
	for r in range(0, num_rows):
		bin_string = str(bin((1 << num_variables) | r))[3:]
		
		for var in range(num_variables):
			table[r][var] = bin_string[var] == '0'
	
	return table

def evaluate(table, expression, variables):
	for row in range(len(table)):
		row_calc = expression
		
		for i, var in enumerate(variables):
			row_calc = re.sub(fr'\b{var}\b', str(table[row][i]), row_calc)
		
		# TODO: doesn't work for everything, need a valid replacement
		row_calc = row_calc.replace('&&', ' and ').replace('||', ' or ').replace('!', 'not ')
		
		try:
			table[row][-1] = eval(row_calc)
		except:
			raise Exception('boolean expression is incorrect or invalid')
	
	return table

def main():
	variables = expression = table = None
	is_first = True
	history = ''
	
	while True:
		variables = expression = table = None
		
		main_break = False
		if is_first:
			print('enter a comma-separated list of the variables to be used')
			print('use singular letters as variable names')
			print('enter "quit" at any time to quit')
		while True:
			temp_list = input('enter the list of variables: ')
			
			if temp_list == 'quit':
				main_break = True
				break
			
			try:
				variables = re.split(r', ?', temp_list)
				
				for var in variables:
					if len(var) != 1:
						raise Exception('use singular letters as variable names')
					elif not var.isalpha():
						raise Exception('only use letters as variable names')
				
				break
			except Exception as e:
				print(e)
		if main_break:
			break
		
		table = get_all_combos(len(variables))
		
		if is_first:
			print('\nonly use the variables previously listed')
			print('enter "back" to go back')
		expression = input('enter the boolean expression: ')
		
		if expression == 'back':
			continue
		elif expression == 'quit':
			break
		
		try:
			table = evaluate(table, expression, variables)
		except Exception as e:
			print(f"\nthe boolean expression is invalid or something else went wrong, try again\n\n{'-' * 30}\n")
			continue
		
		print('\n' + (table_string := pretty_truth_table(table, variables)) + '\n')
		
		history = save(is_first, variables, expression, table_string, history)
		
		print(f"{'-' * 30}\n")
		
		if is_first: is_first = False
	
	print('\n----------\n')
	if history:
		curr_date = datetime.now().strftime('%Y%m%d%H%M%S')
		with open(f"truth_tables-{curr_date}.txt", 'w+') as f:
			f.write(history)
		
		print(f'your files were saved in `truth_tables-{curr_date}.txt`')
	print('buh bye')

if __name__ == '__main__':
	main()