% load all the one meeting case for top 5000 users
% there are four columns in the ONEmeetingCase matrix:
%   [ user a, user b, weighted frequency, 1 - exp measure, friend label ]
load('ONEmeetingCase.mat');
omc = ONEmeetingCase;

disp('Total number of user pairs');
size(omc)
disp('Total number of friend pairs');
sum(omc(:,5)==1)

disp('Sort by weighted frequency');
[~, ind] = sort(omc(:,3));
omc = omc(ind, :);

omc(:,6) = omc(:,3) .* omc(:,4);

prec_rec( omc(:,6), omc(:,5), 'plotROC', 0, 'holdFigure', 1, 'style', 'b:' );
prec_rec( omc(:,3), omc(:,5), 'plotROC', 0, 'holdFigure', 1, 'style', 'r:' );
prec_rec( omc(:,4), omc(:,5), 'plotROC', 0, 'holdFigure', 1, 'style', 'k:' );
