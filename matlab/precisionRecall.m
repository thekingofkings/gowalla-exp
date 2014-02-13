function [prec, recl] = precisionRecall( score, label )

if length(score) ~= length(label)
    error('length of score and label does not match.')
end

n = length(score);
data = [score, label];
[~, ind] = sort(score, 'descend');
data = data(ind, :);

step = max( round(n / 100), 1);
totalPos = sum(data(:,2)==1);

prec = zeros(1,1);
recl = zeros(1,1);

ind = 0;
for i = 1:step:n
    d = data(1:i,:);
    npos = sum(d(:,2)==1);
    prec(ind) = npos / i;
    recl(ind) = npos / totalPos;
    ind = ind + 1;
end

end