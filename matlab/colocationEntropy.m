data = importdata('../data/colocation-diversity');


conditions = [2, 5, 10];
avg_locs = zeros(length(conditions), 2);

for i = 1:length(conditions)
    tmp = data(data(:,1)==conditions(i),2:3);
    numLocs = tmp(:,1:2);
    fri = numLocs(numLocs(:,2)==1, 1);
    nonfri = numLocs(numLocs(:,2)==0, 1);
    avg_locs(i, :) = [mean(fri), mean(nonfri)];
end