import operator
import glob

if __name__ == "__main__":
    best_count = 5

    scores = []

    for path in glob.glob("job.*.out.stat"):
        with open(path) as fp:
            file_contents = fp.read()
            file_generations = file_contents.split('\n\n')

            for generation_block in file_generations:
                if not generation_block:
                    continue

                lines = [line for line in generation_block.split('\n') if line]

                if lines[0] != 'Best Individual of Run:':
                    continue

                fitness_line = lines[3]

                fitness_parts = fitness_line.split(' ')

                score = float(fitness_parts[1][len("Standardized="):])

                tree = lines[-1]

                scores.append((score, tree, path, ))

    scores.sort(key=operator.itemgetter(0))

    best_scores = scores[:best_count]

    for score in best_scores:
        print("Fitness: ", score[0])
        print(score[1])
        print("File: ", score[2])
        print()