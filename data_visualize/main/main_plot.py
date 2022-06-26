import matplotlib.pyplot as plot

dataStr = open("../files/data.txt", "r").readlines()

x = []
y = []

for i in range(len(dataStr)):
    dataStr[i] = dataStr[i].replace("\n", "")
    temp = dataStr[i].split("\t")
    x.append(float(temp[0]))
    y.append(float(temp[1]))

plot.figure(figsize=(20, 8), dpi=80)
plot.xlabel("手机使用时长（小时）", fontsize=22)
plot.ylabel("一月内网购次数", fontsize=22)
plot.title("手机使用时长与网购次数的关系", fontsize=24)
plot.rcParams['font.sans-serif'] = ['Arial Unicode MS']
plot.tick_params(axis='both', which='major', labelsize=20)
plot.scatter(x, y, s=20, c="r")

plot.show()
#plot.savefig("..\\files\\data_plot.png")
