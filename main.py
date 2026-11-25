import random

W, H = 59, 39
CW, CH = 19, 12            # cells: 3*19+1 = 58 <= 59, 3*12+1 = 37 <= 39
g = [[1] * W for _ in range(H)]   # 1 = wall, 0 = floor

def carve_cell(i, j):
    for dy in (1, 2):
        for dx in (1, 2):
            g[3*j + dy][3*i + dx] = 0

def carve_passage(i, j, ni, nj):
    if ni != i:   # horizontal move: open the 1-tile wall column between them
        x = 3 * max(i, ni)
        for dy in (1, 2): g[3*j + dy][x] = 0
    else:         # vertical move
        y = 3 * max(j, nj)
        for dx in (1, 2): g[y][3*i + dx] = 0

seen = {(0, 0)}
stack = [(0, 0)]
carve_cell(0, 0)
while stack:
    i, j = stack[-1]
    nbrs = [(i+1,j),(i-1,j),(i,j+1),(i,j-1)]
    nbrs = [(a,b) for a,b in nbrs if 0 <= a < CW and 0 <= b < CH and (a,b) not in seen]
    if not nbrs:
        stack.pop(); continue
    a, b = random.choice(nbrs)
    carve_cell(a, b); carve_passage(i, j, a, b)
    seen.add((a, b)); stack.append((a, b))

# Optional: knock out a few walls so there are loops (less "one path only")
for _ in range(25):
    i, j = random.randrange(CW-1), random.randrange(CH)
    carve_passage(i, j, i+1, j)

for row in g:
    print(",".join(map(str, row)))
